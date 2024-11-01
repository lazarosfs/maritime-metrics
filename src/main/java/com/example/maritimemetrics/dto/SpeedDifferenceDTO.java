package com.example.maritimemetrics.dto;

import lombok.Getter;

/**
 * Data Transfer Object (DTO) for representing speed differences along with their geographical coordinates.
 */
@Getter
public class SpeedDifferenceDTO {

    /**
     * -- GETTER --
     *  Gets the latitude.
     *
     * @return the latitude of the location
     */
    private final Double latitude;
    /**
     * -- GETTER --
     *  Gets the longitude.
     *
     * @return the longitude of the location
     */
    private final Double longitude;
    /**
     * -- GETTER --
     *  Gets the speed difference.
     *
     * @return the calculated speed difference
     */
    private Double speedDifference;

    /**
     * Constructs a SpeedDifferenceDTO with the specified latitude, longitude, and speed difference.
     *
     * @param latitude        the latitude of the location
     * @param longitude       the longitude of the location
     * @param speedDifference the calculated speed difference
     */
    public SpeedDifferenceDTO(Double latitude, Double longitude, Double speedDifference) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedDifference = speedDifference;
    }

    /**
     * Sets the speed difference.
     *
     * @param speedDifference the calculated speed difference to set
     */
    public void setSpeedDifference(double speedDifference) {
        this.speedDifference = speedDifference; // This method should exist
    }
}
