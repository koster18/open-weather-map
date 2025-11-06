# OpenWeatherMap SDK

SDK for working with OpenWeatherMap API, supporting two operation modes: ON_DEMAND and POLLING.

## Installation

### Maven

Add the dependency to your `pom.xml`:

<dependency>
    <groupId>ru.sterkhovkv</groupId>
    <artifactId>open-weather-map</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>### Requirements

- Java 21 or higher
- Maven 3.6+ (for building the project)

## Quick Start

### Getting SDK Instance

import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;

// Get SDK with API key
OpenWeatherMapSDK sdk = SDKFactory.getInstance("your-api-key", SDKMode.ON_DEMAND);

// Or with custom configuration
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;

SDKConfig config = SDKConfig.builder()
.apiVersion(ApiVersion.V2_5)
.cacheSize(20)
.cacheTtlMinutes(15)
.build();

OpenWeatherMapSDK sdk = SDKFactory.getInstance("your-api-key", SDKMode.ON_DEMAND, config);**Important:** API key can be passed directly or set in the `OPENWEATHER_API_KEY` environment variable. If the key is not provided, SDK will attempt to use the value from the environment variable.

### Getting Weather

import ru.sterkhovkv.openweathermap.model.WeatherResponse;

WeatherResponse weather = sdk.getWeather("Moscow");
System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
System.out.println("Weather: " + weather.getWeather().getDescription());**Note:** When searching for a city by name, SDK returns information about the **first found city** from OpenWeatherMap API search results.

### Resource Cleanup

// Destroy specific instance
sdk.destroy();

// Or through factory (recommended)
SDKFactory.removeInstance("your-api-key");

// Destroy all instances
SDKFactory.removeAllInstances();## Operation Modes

### ON_DEMAND

Data is requested only when `getWeather()` is called. If data exists in cache and is still valid (TTL not expired), it is returned from cache without API request.

OpenWeatherMapSDK sdk = SDKFactory.getInstance("api-key", SDKMode.ON_DEMAND, config);
WeatherResponse weather = sdk.getWeather("Moscow"); // API request if not in cache
WeatherResponse cached = sdk.getWeather("Moscow");   // Return from cache (if TTL not expired)### POLLING

Data is automatically updated in the background for all cities in cache at specified intervals. The `getWeather()` method always returns data from cache (zero latency).

OpenWeatherMapSDK sdk = SDKFactory.getInstance("api-key", SDKMode.POLLING, config);
sdk.getWeather("Moscow"); // Adds city to cache and starts background updates
// Scheduler will automatically update data every pollingIntervalMinutes
// All subsequent getWeather() calls return data from cache instantly
## Working with Multiple API Keys

SDK supports working with multiple API keys simultaneously. A separate SDK instance is created for each unique API key. Attempting to create a second instance with the same API key will return the existing instance (if mode matches) or throw `IllegalSDKStateException` (if mode differs).
a
// Create instances with different API keys
OpenWeatherMapSDK sdk1 = SDKFactory.getInstance("api-key-1", SDKMode.ON_DEMAND);
OpenWeatherMapSDK sdk2 = SDKFactory.getInstance("api-key-2", SDKMode.ON_DEMAND);

// Attempt to create second instance with same key
OpenWeatherMapSDK sdk3 = SDKFactory.getInstance("api-key-1", SDKMode.ON_DEMAND);
// sdk3 == sdk1 (existing instance is returned)

// Attempt to create instance with same key but different mode
OpenWeatherMapSDK sdk4 = SDKFactory.getInstance("api-key-1", SDKMode.POLLING);
// Throws IllegalSDKStateException

// Remove instance by API key
SDKFactory.removeInstance("api-key-1");

// Check if instance exists
boolean exists = SDKFactory.hasInstance("api-key-1");

// Get number of active instances
int count = SDKFactory.getInstanceCount();## Response Structure

SDK returns a `WeatherResponse` object with the following structure (matches requirements):

{
"weather": {
"main": "Clouds",
"description": "scattered clouds"
},
"temperature": {
"temp": 269.6,
"feelsLike": 267.57
},
"visibility": 10000,
"wind": {
"speed": 1.38
},
"datetime": 1675744800,
"sys": {
"sunrise": 1675751262,
"sunset": 1675787560
},
"timezone": 3600,
"name": "Zocca"
}### Field Description

- `weather` - weather condition information
    - `main` - main description (e.g., "Clouds", "Clear")
    - `description` - detailed description
- `temperature` - temperature information
    - `temp` - temperature (default in Kelvin)
    - `feelsLike` - feels like temperature
- `visibility` - visibility in meters
- `wind` - wind information
    - `speed` - wind speed (default in m/s)
- `datetime` - current time as Unix timestamp (UTC)
- `sys` - system information
    - `sunrise` - sunrise time (Unix timestamp)
    - `sunset` - sunset time (Unix timestamp)
- `timezone` - timezone offset in seconds from UTC
- `name` - city name

## Configuration

### SDKConfig

SDKConfig config = SDKConfig.builder()
.apiVersion(ApiVersion.V3_0)              // V3_0 (default) or V2_5
.maxCallsPerDay(2000)                     // Maximum requests per day
.maxCallsPerMinute(60)                    // Maximum requests per minute
.requestTimeoutSeconds(30)                // Request timeout
.connectTimeoutSeconds(10)                // Connection timeout
.cacheSize(10)                            // Cache size (number of cities, default 10)
.cacheTtlMinutes(10)                      // Cache TTL in minutes (default 10)
.pollingIntervalMinutes(10)               // Update interval in POLLING mode
.pollingStrategy(PollingStrategy.STRICT)  // Update strategy
.preemptiveEpsilonMinutes(1)             // Epsilon for PREEMPTIVE_EPSILON strategy
.units(TemperatureUnits.METRIC)           // Units (STANDARD, METRIC, IMPERIAL)
.lang("en")                               // Language for weather descriptions
.build();**Default values (match requirements):**
- `cacheSize`: 10 cities (maximum)
- `cacheTtlMinutes`: 10 minutes (data is considered up-to-date if less than 10 minutes have passed)

### API Versions

- **V2_5** - Current Weather Data API 2.5 (compatible with all API keys)
- **V3_0** - One Call API 3.0 (requires "One Call by Call" subscription, used by default)

### Update Strategies (PollingStrategy)

- **STRICT** - update all cities each tick (default)
- **PREEMPTIVE_EPSILON** - update cities whose TTL expires within epsilon minutes

## Exception Handling

All SDK methods throw exceptions with error reason description:

try {
WeatherResponse weather = sdk.getWeather("Moscow");
} catch (IllegalArgumentException e) {
// Invalid parameters (e.g., null or empty string for city name)
System.err.println("Invalid argument: " + e.getMessage());
} catch (IllegalSDKStateException e) {
// SDK has been destroyed
System.err.println("SDK destroyed: " + e.getMessage());
} catch (CityNotFoundException e) {
// City not found
System.err.println("City not found: " + e.getMessage());
} catch (InvalidApiKeyException e) {
// Invalid API key
System.err.println("Invalid API key: " + e.getMessage());
} catch (ApiRateLimitException e) {
// Rate limit exceeded
System.err.println("Rate limit exceeded: " + e.getMessage());
} catch (NetworkException e) {
// Network error
System.err.println("Network error: " + e.getMessage());
} catch (BadRequestException e) {
// Bad request
System.err.println("Bad request: " + e.getMessage());
e.getInvalidParameters(); // List of invalid parameters
} catch (SDKException e) {
// General SDK error
System.err.println("SDK error: " + e.getMessage());
}## Usage Examples

### Example 1: Basic Usage in ON_DEMAND Mode

import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;

public class BasicExample {
public static void main(String[] args) {
String apiKey = "your-api-key-here";

        // Create configuration
        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();
        
        // Get SDK instance
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND, config);
        
        try {
            // Get weather for one city
            WeatherResponse weather = sdk.getWeather("Moscow");
            
            System.out.println("City: " + weather.getName());
            System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
            System.out.println("Feels like: " + weather.getTemperature().getFeelsLike() + "K");
            System.out.println("Weather: " + weather.getWeather().getMain() + 
                             " - " + weather.getWeather().getDescription());
            System.out.println("Wind speed: " + weather.getWind().getSpeed() + " m/s");
            System.out.println("Visibility: " + weather.getVisibility() + " m");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup resources
            SDKFactory.removeInstance(apiKey);
        }
    }
}### Example 2: Using Cache

public class CacheExample {
public static void main(String[] args) {
String apiKey = "your-api-key-here";
OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

        try {
            // First request - data loaded from API
            long start1 = System.currentTimeMillis();
            WeatherResponse weather1 = sdk.getWeather("London");
            long time1 = System.currentTimeMillis() - start1;
            System.out.println("First request (from API): " + time1 + " ms");
            
            // Second request - data returned from cache (faster)
            long start2 = System.currentTimeMillis();
            WeatherResponse weather2 = sdk.getWeather("London");
            long time2 = System.currentTimeMillis() - start2;
            System.out.println("Second request (from cache): " + time2 + " ms");
            
            System.out.println("Cache size: " + sdk.getCacheSize());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            SDKFactory.removeInstance(apiKey);
        }
    }
}### Example 3: Working with Multiple Cities
va
public class MultipleCitiesExample {
public static void main(String[] args) {
String apiKey = "your-api-key-here";
OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

        try {
            String[] cities = {"Paris", "Tokyo", "New York"};
            
            for (String city : cities) {
                try {
                    WeatherResponse weather = sdk.getWeather(city);
                    System.out.println(city + ": " + 
                                     weather.getTemperature().getTemp() + "K, " +
                                     weather.getWeather().getMain());
                } catch (Exception e) {
                    System.err.println("Failed for " + city + ": " + e.getMessage());
                }
            }
            
            System.out.println("Total cities in cache: " + sdk.getCacheSize());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            SDKFactory.removeInstance(apiKey);
        }
    }
}### Example 4: POLLING Mode

public class PollingExample {
public static void main(String[] args) {
String apiKey = "your-api-key-here";

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .pollingIntervalMinutes(10)  // Update every 10 minutes
            .build();
        
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.POLLING, config);
        
        try {
            // Add cities to cache
            System.out.println("Adding cities to cache...");
            sdk.getWeather("Berlin");
            sdk.getWeather("Madrid");
            System.out.println("Cache size: " + sdk.getCacheSize());
            
            // Scheduler will automatically update data in background
            // All subsequent requests return data from cache instantly
            System.out.println("Polling scheduler started. Data will be updated every 10 minutes.");
            
            // Get data from cache (zero latency)
            WeatherResponse weather = sdk.getWeather("Berlin");
            System.out.println("Berlin weather from cache: " + 
                             weather.getTemperature().getTemp() + "K");
            
            // Wait some time (in real application)
            Thread.sleep(5000);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Stop scheduler and cleanup resources
            SDKFactory.removeInstance(apiKey);
        }
    }
}### Example 5: Working with Multiple API Keys
a
public class MultipleApiKeysExample {
public static void main(String[] args) {
String apiKey1 = "first-api-key";
String apiKey2 = "second-api-key";

        try {
            // Create instances with different keys
            OpenWeatherMapSDK sdk1 = SDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            OpenWeatherMapSDK sdk2 = SDKFactory.getInstance(apiKey2, SDKMode.ON_DEMAND);
            
            // Use different instances
            WeatherResponse weather1 = sdk1.getWeather("Moscow");
            WeatherResponse weather2 = sdk2.getWeather("London");
            
            System.out.println("Active SDK instances: " + SDKFactory.getInstanceCount());
            
            // Attempt to create duplicate with same key returns existing instance
            OpenWeatherMapSDK sdk1Copy = SDKFactory.getInstance(apiKey1, SDKMode.ON_DEMAND);
            System.out.println("sdk1 == sdk1Copy: " + (sdk1 == sdk1Copy)); // true
            
            // Remove instances
            SDKFactory.removeInstance(apiKey1);
            SDKFactory.removeInstance(apiKey2);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}### Example 6: Error Handling
ava
public class ErrorHandlingExample {
public static void main(String[] args) {
String apiKey = "your-api-key-here";
OpenWeatherMapSDK sdk = SDKFactory.getInstance(apiKey, SDKMode.ON_DEMAND);

        try {
            // Attempt to get weather for non-existent city
            try {
                sdk.getWeather("NonExistentCity12345");
            } catch (CityNotFoundException e) {
                System.err.println("City not found: " + e.getMessage());
            }
            
            // Attempt to pass null
            try {
                sdk.getWeather(null);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid argument: " + e.getMessage());
            }
            
        } catch (InvalidApiKeyException e) {
            System.err.println("Invalid API key: " + e.getMessage());
        } catch (ApiRateLimitException e) {
            System.err.println("Rate limit exceeded: " + e.getMessage());
        } catch (NetworkException e) {
            System.err.println("Network error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            SDKFactory.removeInstance(apiKey);
        }
    }
}## Additional Information

### Weather Data Caching

- SDK uses LRU (Least Recently Used) cache for storing weather data
- Maximum cache size: 10 cities (configurable via `cacheSize` in `SDKConfig`)
- Cache TTL: 10 minutes (configurable via `cacheTtlMinutes` in `SDKConfig`)
- When cache limit is reached, the oldest city is removed from cache
- Data is considered up-to-date if less than the configured TTL has passed since last update

### City Coordinates Caching

SDK also uses an internal cache for city coordinates (geocoding):

- **Maximum size**: 100 cities
- **TTL**: 24 hours
- **Normalization**: City names are automatically normalized (converted to lowercase, extra spaces removed) for cache optimization
- **Benefits**:
    - Reduces number of requests to Geocoding API
    - Faster performance for repeated requests for the same cities
    - Saves API rate limits

For example, requests for `"Moscow"`, `"MOSCOW"`, `"  Moscow  "` will use the same cached result.

### Other Features

- In POLLING mode, scheduler automatically starts when SDK is created
- One SDK instance is created per API key (singleton pattern per API key)
- SDK automatically handles API rate limiting

## Full Examples

Full working examples can be found in files:
- `TestOnDemandSDK.java` - example usage in ON_DEMAND mode
- `TestPollingSDK.java` - example usage in POLLING mode
