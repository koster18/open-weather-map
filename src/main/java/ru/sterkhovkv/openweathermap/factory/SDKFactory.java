package ru.sterkhovkv.openweathermap.factory;

import lombok.extern.slf4j.Slf4j;
import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.IllegalSDKStateException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing SDK instances.
 * Implements singleton pattern per API key to prevent duplicate instances.
 */
@Slf4j
public class SDKFactory {

    private static final Map<String, OpenWeatherMapSDK> instances = new ConcurrentHashMap<>();

    private SDKFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets or creates SDK instance for the given API key and mode.
     * If an instance with the same API key already exists, returns the existing instance.
     * If mode differs from existing instance, throws IllegalSDKStateException.
     *
     * @param apiKey API key for OpenWeather API
     * @param mode   SDK operation mode
     * @return SDK instance
     * @throws IllegalSDKStateException if instance with same key exists but different mode
     */
    public static OpenWeatherMapSDK getInstance(String apiKey, SDKMode mode) {
        return getInstance(apiKey, mode, null);
    }

    /**
     * Gets or creates SDK instance for the given API key, mode and configuration.
     * If an instance with the same API key already exists, returns the existing instance.
     * If mode differs from existing instance, throws IllegalSDKStateException.
     * Configuration is only used when creating a new instance.
     *
     * @param apiKey API key for OpenWeather API
     * @param mode   SDK operation mode
     * @param config SDK configuration (null for default)
     * @return SDK instance
     * @throws IllegalSDKStateException if instance with same key exists but different mode
     */
    public static synchronized OpenWeatherMapSDK getInstance(String apiKey, SDKMode mode, SDKConfig config) {
        if (apiKey == null || apiKey.isBlank()) {
            // Try environment variable
            apiKey = System.getenv("OPENWEATHER_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException(
                    "API key is required. Provide it in getInstance() or set OPENWEATHER_API_KEY environment variable"
                );
            }
        }

        if (mode == null) {
            throw new IllegalArgumentException("SDK mode cannot be null");
        }

        // Check if instance already exists
        OpenWeatherMapSDK existingInstance = instances.get(apiKey);
        if (existingInstance != null) {
            // Verify mode matches
            if (existingInstance.getMode() != mode) {
                throw new IllegalSDKStateException(
                    String.format(
                        "SDK instance with API key '%s' already exists with mode %s, " +
                            "but requested mode is %s. Cannot create duplicate instance with different mode.",
                        maskApiKey(apiKey), existingInstance.getMode(), mode
                    )
                );
            }
            log.debug("Returning existing SDK instance for API key: {}", maskApiKey(apiKey));
            return existingInstance;
        }

        // Create new instance
        log.info("Creating new SDK instance for API key: {}, mode: {}", maskApiKey(apiKey), mode);
        OpenWeatherMapSDK newInstance = new OpenWeatherMapSDK(apiKey, mode, config);
        instances.put(apiKey, newInstance);

        return newInstance;
    }

    /**
     * Removes SDK instance for the given API key.
     * Calls destroy() on the instance before removing it.
     *
     * @param apiKey API key
     * @return true if instance was found and removed, false otherwise
     */
    public static synchronized boolean removeInstance(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        OpenWeatherMapSDK instance = instances.remove(apiKey);
        if (instance != null) {
            log.info("Removing SDK instance for API key: {}", maskApiKey(apiKey));
            instance.destroy();
            return true;
        }

        return false;
    }

    /**
     * Checks if SDK instance exists for the given API key.
     *
     * @param apiKey API key
     * @return true if instance exists, false otherwise
     */
    public static boolean hasInstance(String apiKey) {
        return apiKey != null && !apiKey.isBlank() && instances.containsKey(apiKey);
    }

    /**
     * Gets the number of active SDK instances.
     *
     * @return number of active instances
     */
    public static int getInstanceCount() {
        return instances.size();
    }

    /**
     * Removes all SDK instances and destroys them.
     * Should be used for cleanup (e.g., in shutdown hooks).
     */
    public static synchronized void removeAllInstances() {
        log.info("Removing all SDK instances (count: {})", instances.size());

        for (Map.Entry<String, OpenWeatherMapSDK> entry : instances.entrySet()) {
            try {
                entry.getValue().destroy();
            } catch (Exception e) {
                log.error("Error destroying SDK instance for API key: {}", maskApiKey(entry.getKey()), e);
            }
        }

        instances.clear();
        log.info("All SDK instances removed");
    }

    /**
     * Masks API key for logging (shows first 4 characters only).
     *
     * @param apiKey API key
     * @return masked API key
     */
    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****";
    }
}

