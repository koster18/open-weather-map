package ru.sterkhovkv.openweathermap.exception;

/**
 * Base exception for SDK.
 * All SDK exceptions extend this class.
 */
public class SDKException extends RuntimeException {

    public SDKException(String message) {
        super(message);
    }

    public SDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
