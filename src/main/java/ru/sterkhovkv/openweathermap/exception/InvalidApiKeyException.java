package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when API returns 401 - Unauthorized.
 * API key is not provided or does not have access to the API.
 */
public class InvalidApiKeyException extends SDKException {

    public InvalidApiKeyException(String message) {
        super(message);
    }

    public InvalidApiKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
