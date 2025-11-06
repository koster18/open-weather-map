package ru.sterkhovkv.openweathermap.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PollingStrategyTest {

    private static final int EXPECTED_POLLING_STRATEGIES_COUNT = 3;
    private static final String STRICT_NAME = "STRICT";
    private static final String EXPIRED_ONLY_NAME = "EXPIRED_ONLY";
    private static final String PREEMPTIVE_EPSILON_NAME = "PREEMPTIVE_EPSILON";

    @Test
    void testAllValues() {
        PollingStrategy[] values = PollingStrategy.values();
        assertEquals(EXPECTED_POLLING_STRATEGIES_COUNT, values.length);
        assertTrue(Arrays.asList(values).contains(PollingStrategy.STRICT));
        assertTrue(Arrays.asList(values).contains(PollingStrategy.EXPIRED_ONLY));
        assertTrue(Arrays.asList(values).contains(PollingStrategy.PREEMPTIVE_EPSILON));
    }

    @Test
    void testValueOf() {
        assertEquals(PollingStrategy.STRICT, PollingStrategy.valueOf(STRICT_NAME));
        assertEquals(PollingStrategy.EXPIRED_ONLY, PollingStrategy.valueOf(EXPIRED_ONLY_NAME));
        assertEquals(PollingStrategy.PREEMPTIVE_EPSILON, PollingStrategy.valueOf(PREEMPTIVE_EPSILON_NAME));
    }
}
