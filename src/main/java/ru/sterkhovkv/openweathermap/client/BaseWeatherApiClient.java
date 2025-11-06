package ru.sterkhovkv.openweathermap.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.exception.SDKException;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Base class for weather API clients with common logic.
 * Implements Template Method pattern for fetching weather data.
 */
@Slf4j
abstract class BaseWeatherApiClient implements WeatherApiClient {

    protected final WebClient webClient;
    protected final String apiKey;
    protected final SDKConfig config;
    protected final ApiRateLimiter rateLimiter;
    protected final ObjectMapper objectMapper;
    protected final String apiLabel;

    protected BaseWeatherApiClient(
        String apiKey,
        SDKConfig config,
        ApiRateLimiter rateLimiter,
        String baseUrl,
        String apiLabel) {

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
        this.config = config;
        this.rateLimiter = rateLimiter;
        this.objectMapper = new ObjectMapper();
        this.apiLabel = apiLabel;

        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Constants.WEB_CLIENT_BYTE_BUFFER_SIZE))
            .build();
    }

    @Override
    public Object fetchWeather(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }

        rateLimiter.checkAndAcquire();

        try {
            long startTime = System.currentTimeMillis();
            log.debug("Fetching weather data ({}) for coordinates: lat={}, lon={}, units={}, lang={}",
                apiLabel, coordinates.lat(), coordinates.lon(), config.getUnits(), config.getLang());

            String responseBody = executeRequest(coordinates);

            Object weatherData = parseResponse(responseBody);
            validateResponse(weatherData);

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Weather data ({}) fetched successfully for coordinates: lat={}, lon={}, durationMs={}",
                apiLabel, coordinates.lat(), coordinates.lon(), duration);

            return weatherData;

        } catch (SDKException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching weather data ({}) for coordinates: lat={}, lon={}",
                apiLabel, coordinates.lat(), coordinates.lon(), e);
            if (e instanceof TimeoutException) {
                throw new NetworkException("Request timeout while fetching weather data", e);
            }
            throw new NetworkException("Failed to fetch weather data", e);
        }
    }

    /**
     * Executes HTTP request with error handling.
     */
    protected String executeRequest(Coordinates coordinates) {
        String responseBody = webClient.get()
            .uri(uriBuilder -> {
                UriBuilder builder = uriBuilder
                    .path(getEndpoint())
                    .queryParam(Constants.QUERY_PARAM_LAT, coordinates.lat())
                    .queryParam(Constants.QUERY_PARAM_LON, coordinates.lon())
                    .queryParam(Constants.QUERY_PARAM_UNITS, config.getUnits().getApiValue())
                    .queryParam(Constants.QUERY_PARAM_APPID, apiKey);

                if (config.getLang() != null && !config.getLang().isBlank()) {
                    builder.queryParam(Constants.QUERY_PARAM_LANG, config.getLang());
                }

                customizeUri(builder);

                return builder.build();
            })
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(ApiErrorMapper.mapToSDKException(apiLabel,
                    response.statusCode().value(), body))))
            .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new NetworkException(
                apiLabel + " server error: " + response.statusCode()
            )))
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(Constants.DEFAULT_TIMOUT_DURATION))
            .block();

        if (responseBody == null || responseBody.isBlank()) {
            throw new NetworkException("Empty response from " + apiLabel);
        }

        return responseBody;
    }

    /**
     * Parses JSON response to weather data object.
     * Must be implemented by subclasses.
     */
    protected abstract Object parseResponse(String responseBody) throws Exception;

    /**
     * Validates parsed weather data.
     * Must be implemented by subclasses.
     */
    protected abstract void validateResponse(Object weatherData);

    /**
     * Gets API endpoint path.
     * Must be implemented by subclasses.
     */
    protected abstract String getEndpoint();

    /**
     * Allows subclasses to add version-specific query parameters.
     * Default implementation does nothing.
     */
    protected void customizeUri(UriBuilder builder) {
        // Default: no customization
    }
}

