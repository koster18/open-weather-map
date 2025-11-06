package ru.sterkhovkv.openweathermap.client;

import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;
import ru.sterkhovkv.openweathermap.util.Constants;

/**
 * Implementation of WeatherApiClient for OpenWeather Current Weather API 2.5.
 */
public class WeatherApiV2Client extends BaseWeatherApiClient {

    public WeatherApiV2Client(String apiKey, SDKConfig config, ApiRateLimiter rateLimiter) {
        super(apiKey, config, rateLimiter, Constants.WEATHER_API_V2_BASE_URL, "Current Weather API");
    }

    @Override
    protected String getEndpoint() {
        return Constants.WEATHER_API_V2_ENDPOINT;
    }

    @Override
    protected Object parseResponse(String responseBody) throws Exception {
        return objectMapper.readValue(responseBody, WeatherDataV2.class);
    }

    @Override
    protected void validateResponse(Object weatherData) {
        if (!(weatherData instanceof WeatherDataV2 data)) {
            throw new NetworkException("Invalid weather data type in response");
        }
        if (data.getMain() == null) {
            throw new NetworkException("Invalid weather data: main data is null");
        }
    }

    @Override
    public ApiVersion getApiVersion() {
        return ApiVersion.V2_5;
    }
}
