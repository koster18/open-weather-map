package ru.sterkhovkv.openweathermap.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


/**
 * Response model for Geocoding API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponse {
    
    @JsonProperty("lat")
    private Double lat;
    
    @JsonProperty("lon")
    private Double lon;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("state")
    private String state;
}
