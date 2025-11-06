package ru.sterkhovkv.openweathermap.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureUnitsTest {

    private static final String STANDARD_API_VALUE = "standard";
    private static final String METRIC_API_VALUE = "metric";
    private static final String IMPERIAL_API_VALUE = "imperial";
    private static final int EXPECTED_TEMPERATURE_UNITS_COUNT = 3;

    @Test
    void testStandard() {
        assertEquals(STANDARD_API_VALUE, TemperatureUnits.STANDARD.getApiValue());
    }

    @Test
    void testMetric() {
        assertEquals(METRIC_API_VALUE, TemperatureUnits.METRIC.getApiValue());
    }

    @Test
    void testImperial() {
        assertEquals(IMPERIAL_API_VALUE, TemperatureUnits.IMPERIAL.getApiValue());
    }

    @Test
    void testAllValues() {
        TemperatureUnits[] values = TemperatureUnits.values();
        assertEquals(EXPECTED_TEMPERATURE_UNITS_COUNT, values.length);
        assertTrue(Arrays.asList(values).contains(TemperatureUnits.STANDARD));
        assertTrue(Arrays.asList(values).contains(TemperatureUnits.METRIC));
        assertTrue(Arrays.asList(values).contains(TemperatureUnits.IMPERIAL));
    }
}

