package ru.sterkhovkv.openweathermap.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import ru.sterkhovkv.openweathermap.exception.ApiRateLimitException;
import ru.sterkhovkv.openweathermap.exception.BadRequestException;
import ru.sterkhovkv.openweathermap.exception.CityNotFoundException;
import ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException;
import ru.sterkhovkv.openweathermap.exception.SDKException;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to map HTTP error responses to SDKExceptions for OpenWeather APIs.
 */
final class ApiErrorMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ApiErrorMapper() {
    }

    static SDKException mapToSDKException(String apiLabel, int statusCode, String body) {
        try {
            var errorNode = OBJECT_MAPPER.readTree(body);
            String message = errorNode.has(Constants.JSON_FIELD_MESSAGE)
                ? errorNode.get(Constants.JSON_FIELD_MESSAGE).asText()
                : body;

            return switch (HttpStatus.valueOf(statusCode)) {
                case BAD_REQUEST -> {
                    List<String> parameters = null;
                    if (errorNode.has(Constants.JSON_FIELD_PARAMETERS)) {
                        var paramsArray = errorNode.get(Constants.JSON_FIELD_PARAMETERS);
                        parameters = new ArrayList<>();
                        if (paramsArray.isArray()) {
                            for (var param : paramsArray) {
                                parameters.add(param.asText());
                            }
                        }
                    }
                    yield new BadRequestException("Bad request to " + apiLabel + ": " + message, parameters);
                }
                case UNAUTHORIZED -> new InvalidApiKeyException("Invalid API key for " + apiLabel + ": " + message);
                case NOT_FOUND -> new CityNotFoundException("Location not found: " + message);
                case TOO_MANY_REQUESTS ->
                    new ApiRateLimitException("Rate limit exceeded for " + apiLabel + ": " + message);
                default -> new SDKException(apiLabel + " error (" + statusCode + "): " + message);
            };
        } catch (Exception e) {
            return switch (HttpStatus.valueOf(statusCode)) {
                case BAD_REQUEST -> new BadRequestException("Bad request to " + apiLabel + ": " + body);
                case UNAUTHORIZED -> new InvalidApiKeyException("Invalid API key for " + apiLabel);
                case NOT_FOUND -> new CityNotFoundException("Location not found");
                case TOO_MANY_REQUESTS -> new ApiRateLimitException("Rate limit exceeded for " + apiLabel);
                default -> new SDKException(apiLabel + " error (" + statusCode + "): " + body);
            };
        }
    }
}


