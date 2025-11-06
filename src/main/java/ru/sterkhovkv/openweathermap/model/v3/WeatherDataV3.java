package ru.sterkhovkv.openweathermap.model.v3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Internal model for weather data from OpenWeather One Call API 3.0.
 * Maps the API response structure.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDataV3 {

    @JsonProperty("lat")
    private Double latitude;

    @JsonProperty("lon")
    private Double longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("timezone_offset")
    private Integer timezoneOffset;

    @JsonProperty("current")
    private CurrentWeather current;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {

        @JsonProperty("dt")
        private Long datetime;

        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;

        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("pressure")
        private Integer pressure;

        @JsonProperty("humidity")
        private Integer humidity;

        @JsonProperty("dew_point")
        private Double dewPoint;

        @JsonProperty("uvi")
        private Double uvi;

        @JsonProperty("clouds")
        private Integer clouds;

        @JsonProperty("visibility")
        private Integer visibility;

        @JsonProperty("wind_speed")
        private Double windSpeed;

        @JsonProperty("wind_deg")
        private Integer windDeg;

        @JsonProperty("wind_gust")
        private Double windGust;

        @JsonProperty("weather")
        private List<WeatherCondition> weather;

        @JsonProperty("rain")
        private Rain rain;

        @JsonProperty("snow")
        private Snow snow;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherCondition {

        @JsonProperty("id")
        private Integer id;

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        @JsonProperty("icon")
        private String icon;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rain {

        @JsonProperty("1h")
        private Double oneHour;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snow {

        @JsonProperty("1h")
        private Double oneHour;
    }
}
