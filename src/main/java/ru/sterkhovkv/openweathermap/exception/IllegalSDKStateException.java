package ru.sterkhovkv.openweathermap.exception;

/**
 * Exception thrown when attempting to use SDK in an invalid state.
 * For example, attempting to use SDK after calling destroy().
 */
public class IllegalSDKStateException extends SDKException {

    public IllegalSDKStateException(String message) {
        super(message);
    }

    public IllegalSDKStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
