package ru.sterkhovkv.openweathermap.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.CityNotFoundException;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.exception.SDKException;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of GeocodingClient using WebClient.
 * Converts city names to coordinates using OpenWeather Geocoding API.
 */
@Slf4j
public class GeocodingClientImpl implements GeocodingClient {

    private final WebClient webClient;
    private final String apiKey;
    private final ApiRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final Cache<String, Coordinates> geocodeCache;

    public GeocodingClientImpl(String apiKey, SDKConfig config, ApiRateLimiter rateLimiter) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key cannot be null or blank");
        }
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        if (rateLimiter == null) {
            throw new IllegalArgumentException("Rate limiter cannot be null");
        }

        this.apiKey = apiKey;
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
        this.geocodeCache = Caffeine.newBuilder()
            .maximumSize(Constants.GEOCODING_CACHE_MAX_SIZE)
            .expireAfterWrite(Duration.ofHours(Constants.GEOCODING_CACHE_TTL_HOURS))
            .build();

        this.webClient = WebClient.builder()
            .baseUrl(Constants.GEOCODING_API_BASE_URL)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Constants.WEB_CLIENT_BYTE_BUFFER_SIZE))
            .build();
    }

    @Override
    public Coordinates getCoordinates(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or blank");
        }

        String normalizedCity = normalizeCityName(cityName);

        // Local cache lookup
        Coordinates cached = geocodeCache.getIfPresent(normalizedCity);
        if (cached != null) {
            log.debug("Geocoding cache hit for city: {} -> lat={}, lon={}",
                normalizedCity, cached.lat(), cached.lon());
            return cached;
        }

        // Fetch and parse if not in cache
        String responseBody = fetchGeocodingResponse(normalizedCity, cityName);
        Coordinates coordinates = parseAndSelect(cityName, responseBody);
        geocodeCache.put(normalizedCity, coordinates);
        return coordinates;
    }

    private static Coordinates extractCoordinatesFromResponse(String cityName, List<GeocodingResponse> results) {
        if (results == null || results.isEmpty()) {
            throw new CityNotFoundException("City not found: " + cityName);
        }

        GeocodingResponse firstResult = results.getFirst();
        if (firstResult.getLat() == null || firstResult.getLon() == null) {
            throw new CityNotFoundException("Invalid coordinates in response for city: " + cityName);
        }

        return new Coordinates(firstResult.getLat(), firstResult.getLon());
    }

    private String fetchGeocodingResponse(String normalizedCity, String originalCity) {
        // Check rate limit before making request
        rateLimiter.checkAndAcquire();

        try {
            log.debug("Fetching coordinates for city: {} (normalized: {})", originalCity, normalizedCity);

            String responseBody = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(Constants.GEOCODING_DIRECT_ENDPOINT)
                    .queryParam(Constants.QUERY_PARAM_CITY_NAME, normalizedCity)
                    .queryParam(Constants.QUERY_PARAM_LIMIT, Constants.GEOCODING_DEFAULT_LIMIT)
                    .queryParam(Constants.QUERY_PARAM_APPID, apiKey)
                    .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(ApiErrorMapper.mapToSDKException("Geocoding API",
                        response.statusCode().value(), body))))
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new NetworkException(
                    "OpenWeather Geocoding API server error: " + response.statusCode()
                )))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(Constants.DEFAULT_TIMOUT_DURATION))
                .block();

            if (responseBody == null || responseBody.isBlank()) {
                throw new CityNotFoundException("City not found: " + originalCity);
            }
            return responseBody;
        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching coordinates for city: {}", originalCity, e);
            throw new NetworkException("Failed to fetch coordinates for city: " + originalCity, e);
        }
    }

    private Coordinates parseAndSelect(String cityName, String responseBody) {
        try {
            List<GeocodingResponse> results = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, GeocodingResponse.class)
            );
            Coordinates coordinates = extractCoordinatesFromResponse(cityName, results);
            log.debug("Coordinates found for city {}: lat={}, lon={}", cityName, coordinates.lat(), coordinates.lon());
            return coordinates;
        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            throw new NetworkException("Failed to parse geocoding response for city: " + cityName, e);
        }
    }

    private static String normalizeCityName(String raw) {
        String trimmed = raw.trim();
        String collapsed = trimmed.replaceAll("\\s+", " ");
        return collapsed.toLowerCase(Locale.ROOT);
    }
}
