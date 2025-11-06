package ru.sterkhovkv.openweathermap.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    private static final String SDK_ERROR_MESSAGE = "SDK error";
    private static final String CAUSE_MESSAGE = "Cause";
    private static final String CITY_NOT_FOUND_MESSAGE = "City not found";
    private static final String INVALID_API_KEY_MESSAGE = "Invalid API key";
    private static final String RATE_LIMIT_EXCEEDED_MESSAGE = "Rate limit exceeded";
    private static final String NETWORK_ERROR_MESSAGE = "Network error";
    private static final String BAD_REQUEST_MESSAGE = "Bad request";
    private static final String ILLEGAL_SDK_STATE_MESSAGE = "Illegal SDK state";
    private static final String CACHE_ERROR_MESSAGE = "Cache error";
    private static final String TEST_PARAM_1 = "param1";
    private static final String TEST_PARAM_2 = "param2";

    @Test
    void testSDKException() {
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        SDKException exception = new SDKException(SDK_ERROR_MESSAGE, cause);

        assertEquals(SDK_ERROR_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testSDKExceptionWithMessageOnly() {
        SDKException exception = new SDKException(SDK_ERROR_MESSAGE);

        assertEquals(SDK_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testCityNotFoundException() {
        CityNotFoundException exception = new CityNotFoundException(CITY_NOT_FOUND_MESSAGE);

        assertEquals(CITY_NOT_FOUND_MESSAGE, exception.getMessage());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testInvalidApiKeyException() {
        InvalidApiKeyException exception = new InvalidApiKeyException(INVALID_API_KEY_MESSAGE);

        assertEquals(INVALID_API_KEY_MESSAGE, exception.getMessage());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testApiRateLimitException() {
        ApiRateLimitException exception = new ApiRateLimitException(RATE_LIMIT_EXCEEDED_MESSAGE);

        assertEquals(RATE_LIMIT_EXCEEDED_MESSAGE, exception.getMessage());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testNetworkException() {
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        NetworkException exception = new NetworkException(NETWORK_ERROR_MESSAGE, cause);

        assertEquals(NETWORK_ERROR_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testNetworkExceptionWithMessageOnly() {
        NetworkException exception = new NetworkException(NETWORK_ERROR_MESSAGE);

        assertEquals(NETWORK_ERROR_MESSAGE, exception.getMessage());
    }

    @Test
    void testBadRequestException() {
        BadRequestException exception = new BadRequestException(BAD_REQUEST_MESSAGE);

        assertEquals(BAD_REQUEST_MESSAGE, exception.getMessage());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testBadRequestExceptionWithParameters() {
        List<String> parameters = List.of(TEST_PARAM_1, TEST_PARAM_2);

        BadRequestException exception = new BadRequestException(BAD_REQUEST_MESSAGE, parameters);

        assertEquals(BAD_REQUEST_MESSAGE, exception.getMessage());
        assertEquals(parameters, exception.getInvalidParameters());
    }

    @Test
    void testIllegalSDKStateException() {
        IllegalSDKStateException exception = new IllegalSDKStateException(ILLEGAL_SDK_STATE_MESSAGE);

        assertEquals(ILLEGAL_SDK_STATE_MESSAGE, exception.getMessage());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testCacheException() {
        Throwable cause = new RuntimeException(CAUSE_MESSAGE);

        CacheException exception = new CacheException(CACHE_ERROR_MESSAGE, cause);

        assertEquals(CACHE_ERROR_MESSAGE, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof SDKException);
    }

    @Test
    void testCacheExceptionWithMessageOnly() {
        CacheException exception = new CacheException(CACHE_ERROR_MESSAGE);

        assertEquals(CACHE_ERROR_MESSAGE, exception.getMessage());
    }
}

