package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when API returns 429 - Too Many Requests.
 * API key request quota has been exceeded.
 * The request can be retried later or the key quota can be increased.
 */
public class ApiRateLimitException extends SDKException {

    public ApiRateLimitException(String message) {
        super(message);
    }

    public ApiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
