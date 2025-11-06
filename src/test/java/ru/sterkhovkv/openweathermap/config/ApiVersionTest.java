package ru.sterkhovkv.openweathermap.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ApiVersionTest {

    private static final int EXPECTED_API_VERSIONS_COUNT = 2;
    private static final String V2_5_NAME = "V2_5";
    private static final String V3_0_NAME = "V3_0";

    @Test
    void testAllValues() {
        ApiVersion[] values = ApiVersion.values();
        assertEquals(EXPECTED_API_VERSIONS_COUNT, values.length);
        assertTrue(Arrays.asList(values).contains(ApiVersion.V2_5));
        assertTrue(Arrays.asList(values).contains(ApiVersion.V3_0));
    }

    @Test
    void testValueOf() {
        assertEquals(ApiVersion.V2_5, ApiVersion.valueOf(V2_5_NAME));
        assertEquals(ApiVersion.V3_0, ApiVersion.valueOf(V3_0_NAME));
    }
}

