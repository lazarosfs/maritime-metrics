package com.example.maritimemetrics.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a maritime metric entity for storing metrics data related to vessels.
 */
@Entity
@Table(name = "maritime_metrics")
@Data
public class MaritimeMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique code identifying the vessel.
     */
    private String vesselCode;

    /**
     * The date and time when the metric was recorded.
     */
    private LocalDateTime datetime;

    /**
     * The latitude of the vessel's location.
     */
    private Double latitude;

    /**
     * The longitude of the vessel's location.
     */
    private Double longitude;

    /**
     * The power consumption of the vessel.
     */
    private Double power;

    /**
     * The fuel consumption of the vessel.
     */
    private Double fuelConsumption;

    /**
     * The actual speed over ground of the vessel.
     */
    private Double actualSpeedOverground;

    /**
     * The proposed speed over ground for the vessel.
     */
    private Double proposedSpeedOverground;

    /**
     * The predicted fuel consumption of the vessel.
     */
    private Double predictedFuelConsumption;

    /**
     * The calculated speed difference between actual and proposed speeds.
     */
    private Double speedDifference;

    /**
     * Indicates if the metric is invalid.
     */
    private Boolean isInvalid = false;

    /**
     * Indicates if any of the metric values are below zero.
     */
    private Boolean isBelowZero = false;

    /**
     * Indicates if any metric value is missing.
     */
    private Boolean isMissing = false;

    /**
     * Indicates if the metric is considered an outlier.
     */
    private Boolean isOutlier = false;

}
