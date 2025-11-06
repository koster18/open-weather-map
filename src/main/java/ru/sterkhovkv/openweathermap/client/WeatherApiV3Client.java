package ru.sterkhovkv.openweathermap.client;

import org.springframework.web.util.UriBuilder;
import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.model.v3.WeatherDataV3;
import ru.sterkhovkv.openweathermap.util.Constants;

/**
 * Implementation of WeatherApiClient for OpenWeather One Call API 3.0.
 */
public class WeatherApiV3Client extends BaseWeatherApiClient {

    public WeatherApiV3Client(String apiKey, SDKConfig config, ApiRateLimiter rateLimiter) {
        super(apiKey, config, rateLimiter, Constants.WEATHER_API_V3_BASE_URL, "One Call API");
    }

    @Override
    protected String getEndpoint() {
        return Constants.WEATHER_API_V3_ENDPOINT;
    }

    @Override
    protected void customizeUri(UriBuilder builder) {
        builder.queryParam(Constants.QUERY_PARAM_EXCLUDE, Constants.DEFAULT_EXCLUDE_PARAMS);
    }

    @Override
    protected Object parseResponse(String responseBody) throws Exception {
        return objectMapper.readValue(responseBody, WeatherDataV3.class);
    }

    @Override
    protected void validateResponse(Object weatherData) {
        if (!(weatherData instanceof WeatherDataV3 data)) {
            throw new NetworkException("Invalid weather data type in response");
        }
        if (data.getCurrent() == null) {
            throw new NetworkException("Invalid weather data: current weather is null");
        }
    }

    @Override
    public ApiVersion getApiVersion() {
        return ApiVersion.V3_0;
    }
}
