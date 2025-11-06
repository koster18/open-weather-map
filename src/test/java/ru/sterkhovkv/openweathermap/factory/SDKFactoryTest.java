package ru.sterkhovkv.openweathermap.factory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.IllegalSDKStateException;

import static org.junit.jupiter.api.Assertions.*;

class SDKFactoryTest {

    private static final String TEST_API_KEY = "test-api-key-12345";
    private static final String TEST_API_KEY_2 = "test-api-key-67890";
    private static final String NON_EXISTENT_KEY = "non-existent-key";
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "   ";
    private static final int TEST_CACHE_SIZE = 20;
    private static final int EMPTY_INSTANCES_COUNT = 0;
    private static final int SINGLE_INSTANCE_COUNT = 1;
    private static final int TWO_INSTANCES_COUNT = 2;

    @BeforeEach
    @AfterEach
    void cleanup() {
        SDKFactory.removeAllInstances();
    }

    @Test
    void testGetInstanceCreatesNewInstance() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertNotNull(sdk);
        assertEquals(SDKMode.ON_DEMAND, sdk.getMode());
        assertEquals(SINGLE_INSTANCE_COUNT, SDKFactory.getInstanceCount());
    }

    @Test
    void testGetInstanceWithConfig() {
        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .build();

        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertNotNull(sdk);
        assertEquals(SDKMode.ON_DEMAND, sdk.getMode());
    }

    @Test
    void testGetInstanceReturnsSameInstanceForSameKey() {
        OpenWeatherMapSDK sdk1 = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        OpenWeatherMapSDK sdk2 = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertSame(sdk1, sdk2);
        assertEquals(SINGLE_INSTANCE_COUNT, SDKFactory.getInstanceCount());
    }

    @Test
    void testGetInstanceThrowsOnDifferentMode() {
        SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertThrows(IllegalSDKStateException.class, () ->
            SDKFactory.getInstance(TEST_API_KEY, SDKMode.POLLING));
    }

    @Test
    void testGetInstanceThrowsOnNullMode() {
        assertThrows(IllegalArgumentException.class, () ->
            SDKFactory.getInstance(TEST_API_KEY, null));
    }

    @Test
    void testGetInstanceThrowsOnNullApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
            SDKFactory.getInstance(null, SDKMode.ON_DEMAND));
    }

    @Test
    void testGetInstanceThrowsOnBlankApiKey() {
        assertThrows(IllegalArgumentException.class, () ->
            SDKFactory.getInstance(EMPTY_STRING, SDKMode.ON_DEMAND));

        assertThrows(IllegalArgumentException.class, () ->
            SDKFactory.getInstance(BLANK_STRING, SDKMode.ON_DEMAND));
    }

    @Test
    void testMultipleInstancesWithDifferentKeys() {
        OpenWeatherMapSDK sdk1 = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        OpenWeatherMapSDK sdk2 = SDKFactory.getInstance(TEST_API_KEY_2, SDKMode.ON_DEMAND);

        assertNotSame(sdk1, sdk2);
        assertEquals(TWO_INSTANCES_COUNT, SDKFactory.getInstanceCount());
    }

    @Test
    void testRemoveInstance() {
        SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        boolean removed = SDKFactory.removeInstance(TEST_API_KEY);

        assertTrue(removed);
        assertEquals(EMPTY_INSTANCES_COUNT, SDKFactory.getInstanceCount());
        assertFalse(SDKFactory.hasInstance(TEST_API_KEY));
    }

    @Test
    void testRemoveInstanceReturnsFalseForNonExistent() {
        boolean removed = SDKFactory.removeInstance(NON_EXISTENT_KEY);

        assertFalse(removed);
    }

    @Test
    void testRemoveInstanceWithNullKey() {
        boolean removed = SDKFactory.removeInstance(null);

        assertFalse(removed);
    }

    @Test
    void testHasInstance() {
        assertFalse(SDKFactory.hasInstance(TEST_API_KEY));

        SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertTrue(SDKFactory.hasInstance(TEST_API_KEY));
    }

    @Test
    void testHasInstanceWithNullKey() {
        assertFalse(SDKFactory.hasInstance(null));
    }

    @Test
    void testGetInstanceCount() {
        assertEquals(EMPTY_INSTANCES_COUNT, SDKFactory.getInstanceCount());

        SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        assertEquals(SINGLE_INSTANCE_COUNT, SDKFactory.getInstanceCount());

        SDKFactory.getInstance(TEST_API_KEY_2, SDKMode.ON_DEMAND);
        assertEquals(TWO_INSTANCES_COUNT, SDKFactory.getInstanceCount());

        SDKFactory.removeInstance(TEST_API_KEY);
        assertEquals(SINGLE_INSTANCE_COUNT, SDKFactory.getInstanceCount());
    }

    @Test
    void testRemoveAllInstances() {
        SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        SDKFactory.getInstance(TEST_API_KEY_2, SDKMode.ON_DEMAND);

        assertEquals(TWO_INSTANCES_COUNT, SDKFactory.getInstanceCount());

        SDKFactory.removeAllInstances();

        assertEquals(EMPTY_INSTANCES_COUNT, SDKFactory.getInstanceCount());
        assertFalse(SDKFactory.hasInstance(TEST_API_KEY));
        assertFalse(SDKFactory.hasInstance(TEST_API_KEY_2));
    }

    @Test
    void testRemoveAllInstancesWhenEmpty() {
        assertDoesNotThrow(SDKFactory::removeAllInstances);

        assertEquals(EMPTY_INSTANCES_COUNT, SDKFactory.getInstanceCount());
    }
}

