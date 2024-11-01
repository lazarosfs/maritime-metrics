package com.example.maritimemetrics.dto;

import lombok.Getter;

/**
 * Data Transfer Object (DTO) for summarizing metrics related to maritime operations.
 */
@Getter
public class MetricSummaryDTO {

    private Double power;
    private Double fuelConsumption;
    private Double actualSpeedOverground;
    private Double proposedSpeedOverground;
    private Double predictedFuelConsumption;
    private Double speedDifference;

    /**
     * Constructs a MetricSummaryDTO with the specified metrics.
     *
     * @param power the power consumed by the vessel
     * @param fuelConsumption the fuel consumption of the vessel
     * @param actualSpeedOverground the actual speed overground of the vessel
     * @param proposedSpeedOverground the proposed speed overground of the vessel
     * @param predictedFuelConsumption the predicted fuel consumption of the vessel
     * @param speedDifference the difference between actual and proposed speeds
     */
    public MetricSummaryDTO(Double power, Double fuelConsumption, Double actualSpeedOverground,
                            Double proposedSpeedOverground, Double predictedFuelConsumption, Double speedDifference) {
        this.power = power;
        this.fuelConsumption = fuelConsumption;
        this.actualSpeedOverground = actualSpeedOverground;
        this.proposedSpeedOverground = proposedSpeedOverground;
        this.predictedFuelConsumption = predictedFuelConsumption;
        this.speedDifference = speedDifference;
    }
}
