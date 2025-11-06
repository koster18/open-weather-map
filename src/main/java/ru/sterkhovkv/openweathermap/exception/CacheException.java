package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when cache operations fail.
 * For example, cache is full or an error occurred during read/write operations.
 */
public class CacheException extends SDKException {

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
