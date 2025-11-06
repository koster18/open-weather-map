package ru.sterkhovkv.openweathermap.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Internal model for weather data from OpenWeather Current Weather API 2.5.
 * Maps the API response structure.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDataV2 {

    @JsonProperty("coord")
    private Coordinates coord;

    @JsonProperty("weather")
    private List<WeatherCondition> weather;

    @JsonProperty("base")
    private String base;

    @JsonProperty("main")
    private MainData main;

    @JsonProperty("visibility")
    private Integer visibility;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("rain")
    private Rain rain;

    @JsonProperty("snow")
    private Snow snow;

    @JsonProperty("clouds")
    private Clouds clouds;

    @JsonProperty("dt")
    private Long datetime;

    @JsonProperty("sys")
    private SystemData sys;

    @JsonProperty("timezone")
    private Integer timezone;

    @JsonProperty("id")
    private Integer cityId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("cod")
    private Integer cod;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coordinates {

        @JsonProperty("lon")
        private Double lon;

        @JsonProperty("lat")
        private Double lat;
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
    public static class MainData {

        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("pressure")
        private Integer pressure;

        @JsonProperty("humidity")
        private Integer humidity;

        @JsonProperty("temp_min")
        private Double tempMin;

        @JsonProperty("temp_max")
        private Double tempMax;

        @JsonProperty("sea_level")
        private Integer seaLevel;

        @JsonProperty("grnd_level")
        private Integer grndLevel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {

        @JsonProperty("speed")
        private Double speed;

        @JsonProperty("deg")
        private Integer deg;

        @JsonProperty("gust")
        private Double gust;
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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Clouds {

        @JsonProperty("all")
        private Integer all;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemData {

        @JsonProperty("type")
        private Integer type;

        @JsonProperty("id")
        private Integer id;

        @JsonProperty("country")
        private String country;

        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;
    }
}
