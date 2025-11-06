package ru.sterkhovkv.openweathermap.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when API returns 400 - Bad Request.
 * Required parameters are missing or parameters have incorrect format.
 */
@Getter
public class BadRequestException extends SDKException {

    /**
     * -- GETTER --
     * Returns the list of parameters that caused the error.
     */
    private final List<String> invalidParameters;

    public BadRequestException(String message) {
        super(message);
        this.invalidParameters = List.of();
    }

    public BadRequestException(String message, List<String> invalidParameters) {
        super(message);
        this.invalidParameters = invalidParameters != null ? invalidParameters : List.of();
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.invalidParameters = List.of();
    }

    public BadRequestException(String message, List<String> invalidParameters, Throwable cause) {
        super(message, cause);
        this.invalidParameters = invalidParameters != null ? invalidParameters : List.of();
    }
}
