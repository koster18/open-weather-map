package ru.sterkhovkv.openweathermap.model;

/**
 * Geographic coordinates (latitude and longitude).
 *
 * @param lat Latitude in decimal degrees (-90 to 90).
 * @param lon Longitude in decimal degrees (-180 to 180).
 */
public record Coordinates(

    double lat,

    double lon

) {
}
