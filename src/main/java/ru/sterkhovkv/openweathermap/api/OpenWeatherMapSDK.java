package ru.sterkhovkv.openweathermap.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.sterkhovkv.openweathermap.cache.WeatherCache;
import ru.sterkhovkv.openweathermap.cache.LRUWeatherCache;
import ru.sterkhovkv.openweathermap.client.GeocodingClient;
import ru.sterkhovkv.openweathermap.client.GeocodingClientImpl;
import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.IllegalSDKStateException;
import ru.sterkhovkv.openweathermap.exception.SDKException;
import ru.sterkhovkv.openweathermap.exception.CityNotFoundException;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.exception.ApiRateLimitException;
import ru.sterkhovkv.openweathermap.exception.BadRequestException;
import ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException;
import ru.sterkhovkv.openweathermap.model.CacheEntry;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;
import ru.sterkhovkv.openweathermap.model.WeatherResponseMapper;
import ru.sterkhovkv.openweathermap.scheduler.WeatherPollingScheduler;
import ru.sterkhovkv.openweathermap.scheduler.PollingSchedulerConfig;
import ru.sterkhovkv.openweathermap.util.Constants;

/**
 * Main SDK class for OpenWeatherMap API.
 * Provides methods to retrieve weather data for cities.
 */
@Slf4j
public class OpenWeatherMapSDK {
    
    private final String apiKey;
    /**
     * -- GETTER --
     *  Gets the SDK operation mode.
     */
    @Getter
    private final SDKMode mode;
    private final SDKConfig config;
    private final WeatherCache cache;
    private final GeocodingClient geocodingClient;
    private final WeatherApiClient weatherApiClient;
    private final ApiRateLimiter rateLimiter;
    private final ApiVersion apiVersion;
    private WeatherPollingScheduler pollingScheduler;
    private volatile boolean destroyed = false;
    
    /**
     * Creates a new SDK instance.
     * Should not be called directly. Use SDKFactory.getInstance() instead.
     *
     * @param apiKey API key for OpenWeather API
     * @param mode SDK operation mode
     * @param config SDK configuration
     */
    public OpenWeatherMapSDK(String apiKey, SDKMode mode, SDKConfig config) {
        this.apiKey = ApiKeyResolver.resolve(apiKey);
        this.mode = validateMode(mode);
        this.config = (config != null ? config : SDKConfig.defaultConfig()).validate();
        
        this.rateLimiter = initializeRateLimiter(this.config);
        this.cache = initializeCache(this.config);
        this.apiVersion = this.config.getApiVersion();
        this.geocodingClient = initializeGeocodingClient(this.apiKey, this.config, this.rateLimiter);
        this.weatherApiClient = initializeWeatherApiClient(this.apiVersion, this.apiKey, this.config, this.rateLimiter);
        
        log.info("Using API version: {}", this.apiVersion);
        
        if (this.mode == SDKMode.POLLING) {
            this.pollingScheduler = initializePollingScheduler(this.config);
            log.info("SDK initialized in POLLING mode");
        } else {
            log.info("SDK initialized in ON_DEMAND mode");
        }
    }
    
    private static SDKMode validateMode(SDKMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("SDK mode cannot be null");
        }
        return mode;
    }
    
    private static ApiRateLimiter initializeRateLimiter(SDKConfig config) {
        return new ApiRateLimiter(
            config.getMaxCallsPerDay(),
            config.getMaxCallsPerMinute()
        );
    }
    
    private static WeatherCache initializeCache(SDKConfig config) {
        return new LRUWeatherCache(
            config.getCacheSize(),
            config.getCacheTtlMinutes()
        );
    }
    
    private static GeocodingClient initializeGeocodingClient(String apiKey, SDKConfig config,
                                                             ApiRateLimiter rateLimiter) {
        return new GeocodingClientImpl(apiKey, config, rateLimiter);
    }
    
    private static WeatherApiClient initializeWeatherApiClient(
            ApiVersion apiVersion,
            String apiKey,
            SDKConfig config,
            ApiRateLimiter rateLimiter) {
        return WeatherApiClientFactory.create(apiVersion, apiKey, config, rateLimiter);
    }
    
    private WeatherPollingScheduler initializePollingScheduler(SDKConfig config) {
        PollingSchedulerConfig schedulerConfig = createPollingSchedulerConfig(config);
        WeatherPollingScheduler scheduler = new WeatherPollingScheduler(schedulerConfig);
        scheduler.start();
        return scheduler;
    }
    
    private PollingSchedulerConfig createPollingSchedulerConfig(SDKConfig config) {
        return new PollingSchedulerConfig(
            cache,
            weatherApiClient,
            apiVersion,
            config.getPollingIntervalMinutes(),
            config.getCacheTtlMinutes(),
            config.getPollingStrategy(),
            config.getPreemptiveEpsilonMinutes()
        );
    }
    
    /**
     * Gets weather data for a city.
     * Returns data from cache if available and valid, otherwise fetches from API.
     *
     * @param cityName city name
     * @return weather response
     * @throws IllegalArgumentException if city name is null or blank
     * @throws IllegalSDKStateException if SDK has been destroyed
     * @throws CityNotFoundException if city not found
     * @throws NetworkException if network error occurs
     * @throws ApiRateLimitException if API rate limit exceeded
     * @throws BadRequestException if request is invalid (missing or incorrect parameters)
     * @throws InvalidApiKeyException if API key is invalid or unauthorized
     * @throws SDKException if any other SDK error occurs
     */
    public WeatherResponse getWeather(String cityName) {
        checkNotDestroyed();
        validateCityName(cityName);
        
        long currentTime = System.currentTimeMillis();
        long ttlMillis = config.getCacheTtlMinutes() * Constants.MILLIS_PER_MINUTE;
        
        WeatherResponse cached = getCachedWeather(cityName, currentTime, ttlMillis);
        if (cached != null) {
            return cached;
        }
        
        return fetchAndCacheWeather(cityName, currentTime);
    }
    
    private static void validateCityName(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or blank");
        }
    }
    
    private WeatherResponse getCachedWeather(String cityName, long currentTime, long ttlMillis) {
        CacheEntry cachedEntry = cache.get(cityName);
        if (cachedEntry != null && cachedEntry.isValid(currentTime, ttlMillis)) {
            log.debug("Returning cached weather data for city: {}", cityName);
            return WeatherResponseMapper.toResponse(
                cachedEntry.weatherData(),
                cachedEntry.apiVersion(),
                cityName
            );
        }
        return null;
    }
    
    private WeatherResponse fetchAndCacheWeather(String cityName, long currentTime) {
        log.debug("Cache miss for city: {}, fetching from API (version: {})", cityName, apiVersion);
        
        try {
            Coordinates coordinates = geocodingClient.getCoordinates(cityName);
            Object weatherData = weatherApiClient.fetchWeather(coordinates);
            cache.put(cityName, coordinates, weatherData, apiVersion, currentTime);
            
            if (mode == SDKMode.POLLING) {
                log.debug("City {} cached and will be updated by polling scheduler", cityName);
            }
            
            return WeatherResponseMapper.toResponse(weatherData, apiVersion, cityName);
            
        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting weather for city: {}", cityName, e);
            throw new SDKException("Failed to get weather for city: " + cityName, e);
        }
    }
    
    /**
     * Destroys the SDK instance.
     * Stops polling scheduler and cleans up resources.
     * After calling this method, the SDK cannot be used.
     */
    public void destroy() {
        if (destroyed) {
            log.warn("SDK instance already destroyed");
            return;
        }
        
        destroyed = true;
        log.info("Destroying SDK instance...");
        
        try {
            stopPollingScheduler();
            clearCache();
            log.info("SDK instance destroyed");
        } catch (Exception e) {
            log.error("Error during SDK destruction", e);
            throw e;
        }
    }
    
    private void stopPollingScheduler() {
        if (pollingScheduler != null) {
            try {
                pollingScheduler.stop();
            } catch (Exception e) {
                log.error("Error stopping polling scheduler", e);
            } finally {
                pollingScheduler = null;
            }
        }
    }
    
    private void clearCache() {
        try {
            cache.clear();
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }
    
    /**
     * Checks if SDK has been destroyed.
     *
     * @throws IllegalSDKStateException if SDK has been destroyed
     */
    private void checkNotDestroyed() {
        if (destroyed) {
            throw new IllegalSDKStateException("SDK instance has been destroyed");
        }
    }
    
    /**
     * Gets the API key used by this SDK instance.
     *
     * @return API key (masked for security)
     */
    public String getApiKey() {
        if (apiKey == null || apiKey.length() <= 4) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****";
    }

    /**
     * Gets current cache size.
     *
     * @return number of cities in cache
     */
    public int getCacheSize() {
        return cache.size();
    }
}

