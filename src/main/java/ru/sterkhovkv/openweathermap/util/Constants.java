package ru.sterkhovkv.openweathermap.util;

/**
 * Constants for OpenWeatherMap SDK.
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // API Base URLs
    public static final String GEOCODING_API_BASE_URL = "http://api.openweathermap.org/geo/1.0";
    public static final String WEATHER_API_V2_BASE_URL = "https://api.openweathermap.org/data/2.5";
    public static final String WEATHER_API_V3_BASE_URL = "https://api.openweathermap.org/data/3.0";

    // Geocoding API endpoints
    public static final String GEOCODING_DIRECT_ENDPOINT = "/direct";

    // Weather API endpoints
    public static final String WEATHER_API_V2_ENDPOINT = "/weather";
    public static final String WEATHER_API_V3_ENDPOINT = "/onecall";

    // Query parameters
    public static final String QUERY_PARAM_LAT = "lat";
    public static final String QUERY_PARAM_LON = "lon";
    public static final String QUERY_PARAM_CITY_NAME = "q";
    public static final String QUERY_PARAM_LIMIT = "limit";
    public static final String QUERY_PARAM_APPID = "appid";
    public static final String QUERY_PARAM_EXCLUDE = "exclude";
    public static final String QUERY_PARAM_UNITS = "units";
    public static final String QUERY_PARAM_LANG = "lang";

    // One Call API exclude values
    public static final String EXCLUDE_MINUTELY = "minutely";
    public static final String EXCLUDE_HOURLY = "hourly";
    public static final String EXCLUDE_DAILY = "daily";
    public static final String EXCLUDE_ALERTS = "alerts";

    // Default exclude parameter value (exclude all except current)
    public static final String DEFAULT_EXCLUDE_PARAMS =
        EXCLUDE_MINUTELY + "," + EXCLUDE_HOURLY + "," + EXCLUDE_DAILY + "," + EXCLUDE_ALERTS;

    // Geocoding API defaults
    public static final int GEOCODING_DEFAULT_LIMIT = 1;
    public static final int GEOCODING_CACHE_MAX_SIZE = 100;
    public static final int GEOCODING_CACHE_TTL_HOURS = 24;

    // Web Client properties
    public static final int WEB_CLIENT_BYTE_BUFFER_SIZE = 1024 * 1024;
    public static final int DEFAULT_TIMOUT_DURATION = 30;

    // Scheduler shutdown timeout
    public static final int SCHEDULER_SHUTDOWN_TIMEOUT_SECONDS = 30;

    // Time conversion
    public static final long MILLIS_PER_MINUTE = 60 * 1000;
    public static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    // JSON field names
    public static final String JSON_FIELD_MESSAGE = "message";
    public static final String JSON_FIELD_PARAMETERS = "parameters";
}

