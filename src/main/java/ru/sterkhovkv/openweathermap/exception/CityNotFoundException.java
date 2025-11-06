package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when API returns 404 - Not Found.
 * City is not found in the service database.
 * The same request should not be retried.
 */
public class CityNotFoundException extends SDKException {

    public CityNotFoundException(String message) {
        super(message);
    }

    public CityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
