package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when API returns 5xx errors or network errors occur.
 * Internal OpenWeather server errors or network issues.
 * The request can be retried.
 */
public class NetworkException extends SDKException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
