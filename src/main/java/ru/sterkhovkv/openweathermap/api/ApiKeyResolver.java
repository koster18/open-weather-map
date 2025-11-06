package ru.sterkhovkv.openweathermap.api;

import ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException;

/**
 * Utility for resolving and validating API keys.
 */
final class ApiKeyResolver {
    
    private static final String ENV_VAR_NAME = "OPENWEATHER_API_KEY";
    
    private ApiKeyResolver() {}
    
    /**
     * Resolves API key from provided value or environment variable.
     * Validates that the key is not null or blank.
     *
     * @param providedKey API key provided by user (can be null)
     * @return validated API key
     * @throws InvalidApiKeyException if key cannot be resolved
     */
    static String resolve(String providedKey) {
        if (providedKey != null && !providedKey.isBlank()) {
            return providedKey;
        }
        
        String envKey = System.getenv(ENV_VAR_NAME);
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }
        
        throw new InvalidApiKeyException(
            "API key is required. Provide it in constructor or set " + ENV_VAR_NAME + " environment variable"
        );
    }
}

