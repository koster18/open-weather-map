package ru.sterkhovkv.openweathermap;

import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;

/**
 * Manual test for SDK in POLLING mode.
 * Replace YOUR_API_KEY with your actual OpenWeather API key.
 */
public class TestPollingSDK {
    
    private static final String YOUR_API_KEY = "YOUR_API_KEY";
    
    private static final String CITY_BERLIN = "Berlin";
    private static final String CITY_MADRID = "Madrid";
    
    private static final long POLLING_WAIT_SECONDS = 5;
    
    public static void main(String[] args) {
        System.out.println("=== OpenWeatherMap SDK Test - POLLING Mode ===\n");
        
        String apiKey = YOUR_API_KEY;
        
        if (apiKey == null || apiKey.equals("YOUR_API_KEY") || apiKey.isBlank()) {
            System.err.println("ERROR: Please set YOUR_API_KEY in TestPollingSDK.java or set OPENWEATHER_API_KEY environment variable");
            System.exit(1);
        }
        
        try {
            SDKConfig config = SDKConfig.builder()
                .apiVersion(ApiVersion.V2_5)
                .build();
            
            testPollingMode(apiKey, config);
            
            System.out.println("\n=== Test completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            SDKFactory.removeInstance(apiKey);
        }
    }
    
    private static void testPollingMode(String apiKey, SDKConfig config) {
        System.out.println("--- Test: POLLING Mode ---");
        
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.POLLING, config);
        
        try {
            System.out.println("Adding cities to cache...");
            sdk.getWeather(CITY_BERLIN);
            sdk.getWeather(CITY_MADRID);
            System.out.println("Cache size: " + sdk.getCacheSize());
            
            System.out.println("Waiting " + POLLING_WAIT_SECONDS + " seconds (polling updates every 10 minutes)...");
            try {
                Thread.sleep(POLLING_WAIT_SECONDS * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            WeatherResponse weather = sdk.getWeather(CITY_BERLIN);
            System.out.println("Berlin weather from cache: " + weather.getTemperature().getTemp() + "K");
            System.out.println();
            
            sdk.destroy();
            System.out.println("SDK destroyed");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Failed to test polling: " + e.getMessage());
            throw e;
        }
    }
}

