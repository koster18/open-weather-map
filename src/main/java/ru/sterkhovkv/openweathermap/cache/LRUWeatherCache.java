package ru.sterkhovkv.openweathermap.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.exception.CacheException;
import ru.sterkhovkv.openweathermap.model.CacheEntry;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * LRU cache implementation for weather data using Caffeine.
 * Features:
 * - Maximum size: 10 entries (as per requirements)
 * - TTL: configurable (default 10 minutes)
 * - LRU eviction policy
 * - Thread-safe
 */
@Slf4j
public class LRUWeatherCache implements WeatherCache {
    
    private final Cache<String, CacheEntry> cache;
    private final long ttlMillis;
    
    /**
     * Creates a new cache instance.
     *
     * @param maxSize maximum number of entries (default: 10)
     * @param ttlMinutes time-to-live in minutes (default: 10)
     */
    public LRUWeatherCache(int maxSize, long ttlMinutes) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        if (ttlMinutes <= 0) {
            throw new IllegalArgumentException("ttlMinutes must be positive");
        }
        
        this.ttlMillis = ttlMinutes * Constants.MILLIS_PER_MINUTE;
        
        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
            .removalListener((key, value, cause) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Cache entry removed: city={}, cause={}", key, cause);
                }
            })
            .build();
    }
    
    @Override
    public CacheEntry get(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }
        
        try {
            CacheEntry entry = cache.getIfPresent(cityName);
            if (entry == null) {
                return null;
            }
            
            // Additional TTL check (Caffeine also checks, but we verify explicitly)
            long currentTime = System.currentTimeMillis();
            if (!entry.isValid(currentTime, ttlMillis)) {
                cache.invalidate(cityName);
                return null;
            }
            
            return entry;
        } catch (Exception e) {
            log.error("Error getting cache entry for city: {}", cityName, e);
            throw new CacheException("Failed to get cache entry", e);
        }
    }
    
    @Override
    public void put(String cityName, Coordinates coordinates, Object weatherData,
                    ApiVersion apiVersion, long timestamp) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or blank");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("API version cannot be null");
        }
        
        try {
            CacheEntry entry = new CacheEntry(cityName, coordinates, weatherData, apiVersion, timestamp);
            cache.put(cityName, entry);
            
            if (log.isDebugEnabled()) {
                log.debug("Cache entry added: city={}, apiVersion={}, timestamp={}",
                    cityName, apiVersion, timestamp);
            }
        } catch (Exception e) {
            log.error("Error putting cache entry for city: {}", cityName, e);
            throw new CacheException("Failed to put cache entry", e);
        }
    }
    
    @Override
    public void update(String cityName, Object weatherData, ApiVersion apiVersion, long timestamp) {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or blank");
        }
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("API version cannot be null");
        }
        
        try {
            CacheEntry existingEntry = cache.getIfPresent(cityName);
            if (existingEntry == null) {
                throw new CacheException("Cannot update non-existent cache entry for city: " + cityName);
            }
            
            CacheEntry updatedEntry = new CacheEntry(
                cityName,
                existingEntry.coordinates(),
                weatherData,
                apiVersion,
                timestamp
            );
            cache.put(cityName, updatedEntry);
            
            if (log.isDebugEnabled()) {
                log.debug("Cache entry updated: city={}, apiVersion={}, timestamp={}",
                    cityName, apiVersion, timestamp);
            }
        } catch (Exception e) {
            log.error("Error updating cache entry for city: {}", cityName, e);
            throw new CacheException("Failed to update cache entry", e);
        }
    }
    
    @Override
    public boolean isValid(String cityName, long currentTime, long ttlMillis) {
        if (cityName == null || cityName.isBlank()) {
            return false;
        }
        
        CacheEntry entry = cache.getIfPresent(cityName);
        if (entry == null) {
            return false;
        }
        
        return entry.isValid(currentTime, ttlMillis);
    }
    
    @Override
    public Coordinates getCoordinates(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }
        
        CacheEntry entry = cache.getIfPresent(cityName);
        return entry != null ? entry.coordinates() : null;
    }
    
    @Override
    public List<String> getAllCities() {
        try {
            return new ArrayList<>(cache.asMap().keySet());
        } catch (Exception e) {
            log.error("Error getting all cities from cache", e);
            throw new CacheException("Failed to get all cities", e);
        }
    }
    
    @Override
    public void remove(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return;
        }
        
        try {
            cache.invalidate(cityName);
            if (log.isDebugEnabled()) {
                log.debug("Cache entry removed: city={}", cityName);
            }
        } catch (Exception e) {
            log.error("Error removing cache entry for city: {}", cityName, e);
            throw new CacheException("Failed to remove cache entry", e);
        }
    }
    
    @Override
    public void clear() {
        try {
            cache.invalidateAll();
            if (log.isDebugEnabled()) {
                log.debug("Cache cleared");
            }
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            throw new CacheException("Failed to clear cache", e);
        }
    }
    
    @Override
    public int size() {
        return (int) cache.estimatedSize();
    }
}

