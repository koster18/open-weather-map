package ru.sterkhovkv.openweathermap;

import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;

/**
 * Manual test for SDK in ON_DEMAND mode.
 * Replace YOUR_API_KEY with your actual OpenWeather API key.
 */
public class TestOnDemandSDK {
    
    private static final String YOUR_API_KEY = "YOUR_API_KEY";
    
    private static final String CITY_MOSCOW = "Moscow";
    private static final String CITY_LONDON = "London";
    private static final String CITY_PARIS = "Paris";
    private static final String CITY_TOKYO = "Tokyo";
    private static final String CITY_NEW_YORK = "New York";
    
    public static void main(String[] args) {
        System.out.println("=== OpenWeatherMap SDK Test - ON_DEMAND Mode ===\n");
        
        String apiKey = YOUR_API_KEY;
        
        if (apiKey == null || apiKey.equals("YOUR_API_KEY") || apiKey.isBlank()) {
            System.err.println("ERROR: Please set YOUR_API_KEY in TestOnDemandSDK.java or set OPENWEATHER_API_KEY environment variable");
            System.exit(1);
        }
        
        try {
            SDKConfig config = SDKConfig.builder()
                .apiVersion(ApiVersion.V2_5)
                .build();
            
            testSingleCity(apiKey, config);
            testCache(apiKey, config);
            testMultipleCities(apiKey, config);
            
            System.out.println("\n=== All tests completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            SDKFactory.removeInstance(apiKey);
        }
    }
    
    private static void testSingleCity(String apiKey, SDKConfig config) {
        System.out.println("--- Test 1: Single City Request ---");
        
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND, config);
        
        try {
            System.out.println("Requesting weather for: " + CITY_MOSCOW);
            WeatherResponse weather = sdk.getWeather(CITY_MOSCOW);
            
            System.out.println("Success!");
            System.out.println("City: " + weather.getName());
            System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
            System.out.println("Feels like: " + weather.getTemperature().getFeelsLike() + "K");
            System.out.println("Weather: " + weather.getWeather().getMain() + " - " + weather.getWeather().getDescription());
            System.out.println("Wind speed: " + weather.getWind().getSpeed() + " m/s");
            System.out.println("Visibility: " + weather.getVisibility() + " m");
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Failed to get weather: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testCache(String apiKey, SDKConfig config) {
        System.out.println("--- Test 2: Cache Test ---");
        
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND, config);
        
        try {
            long start1 = System.currentTimeMillis();
            sdk.getWeather(CITY_LONDON);
            long time1 = System.currentTimeMillis() - start1;
            System.out.println("First request (from API): " + time1 + " ms");
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long start2 = System.currentTimeMillis();
            sdk.getWeather(CITY_LONDON);
            long time2 = System.currentTimeMillis() - start2;
            System.out.println("Second request (from cache): " + time2 + " ms");
            System.out.println("Cache size: " + sdk.getCacheSize());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Failed to test cache: " + e.getMessage());
            throw e;
        }
    }
    
    private static void testMultipleCities(String apiKey, SDKConfig config) {
        System.out.println("--- Test 3: Multiple Cities ---");
        
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND, config);
        
        String[] cities = {CITY_PARIS, CITY_TOKYO, CITY_NEW_YORK};
        
        for (String city : cities) {
            try {
                System.out.println("Requesting weather for: " + city);
                WeatherResponse weather = sdk.getWeather(city);
                System.out.println("  Temperature: " + weather.getTemperature().getTemp() + "K");
                System.out.println("  Weather: " + weather.getWeather().getMain());
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("  Failed for " + city + ": " + e.getMessage());
            }
        }
        
        System.out.println("Cache size after all requests: " + sdk.getCacheSize());
        System.out.println();
    }
}

