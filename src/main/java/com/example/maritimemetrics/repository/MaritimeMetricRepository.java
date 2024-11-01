package com.example.maritimemetrics.repository;

import com.example.maritimemetrics.dto.MetricSummaryDTO;
import com.example.maritimemetrics.dto.SpeedDifferenceDTO;
import com.example.maritimemetrics.model.MaritimeMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for accessing and manipulating maritime metric data.
 */
@Repository
public interface MaritimeMetricRepository extends JpaRepository<MaritimeMetric, Long> {

    /**
     * Finds all maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCode(String vesselCode);

    /**
     * Finds all valid maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of valid maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCodeAndIsInvalidFalse(String vesselCode);

    /**
     * Finds all invalid maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of invalid maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCodeAndIsInvalidTrue(String vesselCode);

    /**
     * Finds all missing maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of missing maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCodeAndIsMissingTrue(String vesselCode);

    /**
     * Finds all below-zero maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of below-zero maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCodeAndIsBelowZeroTrue(String vesselCode);

    /**
     * Finds all outlier maritime metrics for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of outlier maritime metrics for the specified vessel
     */
    List<MaritimeMetric> findAllByVesselCodeAndIsOutlierTrue(String vesselCode);

    /**
     * Retrieves all unique vessel codes from the maritime metrics data.
     *
     * @return a list of unique vessel codes
     */
    @Query("SELECT DISTINCT m.vesselCode FROM MaritimeMetric m")
    List<String> findAllVesselCodes();  // Get all unique vessel codes

    /**
     * Finds speed differences for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @return a list of speed difference data transfer objects for the specified vessel
     */
    @Query("SELECT new com.example.maritimemetrics.dto.SpeedDifferenceDTO(m.latitude, m.longitude, m.speedDifference) " +
            "FROM MaritimeMetric m WHERE m.vesselCode = :vesselCode AND m.isInvalid = false AND m.speedDifference IS NOT NULL")
    List<SpeedDifferenceDTO> findSpeedDifferencesByVesselCode(String vesselCode);

    /**
     * Finds metrics for a specific vessel code within a specified date range.
     *
     * @param vesselCode the code of the vessel
     * @param startDate  the start date for the date range
     * @param endDate    the end date for the date range
     * @return a list of metric summary data transfer objects for the specified vessel and date range
     */
    @Query("SELECT new com.example.maritimemetrics.dto.MetricSummaryDTO(m.power, m.fuelConsumption, m.actualSpeedOverground, m.proposedSpeedOverground, m.predictedFuelConsumption, m.speedDifference) " +
            "FROM MaritimeMetric m WHERE m.vesselCode = :vesselCode AND m.isInvalid = false AND m.datetime BETWEEN :startDate AND :endDate")
    List<MetricSummaryDTO> findMetricsByVesselCodeAndDatetimeBetween(String vesselCode, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds speed outliers for a specific vessel based on a given threshold.
     *
     * @param vesselCode    the code of the vessel
     * @param speedThreshold the threshold for identifying speed outliers
     * @return a list of maritime metrics that are considered speed outliers
     */
    @Query("SELECT m FROM MaritimeMetric m WHERE m.vesselCode = :vesselCode AND m.isInvalid = true AND " +
            "ABS(m.actualSpeedOverground - m.proposedSpeedOverground) / m.proposedSpeedOverground > :speedThreshold")
    List<MaritimeMetric> findSpeedOutliersByVesselCode(String vesselCode, double speedThreshold);

    /**
     * Finds fuel consumption outliers for a specific vessel based on a given threshold.
     *
     * @param vesselCode    the code of the vessel
     * @param fuelThreshold the threshold for identifying fuel consumption outliers
     * @return a list of maritime metrics that are considered fuel consumption outliers
     */
    @Query("SELECT m FROM MaritimeMetric m WHERE m.vesselCode = :vesselCode AND m.isInvalid = true AND " +
            "ABS(m.fuelConsumption - m.predictedFuelConsumption) / m.predictedFuelConsumption > :fuelThreshold")
    List<MaritimeMetric> findFuelOutliersByVesselCode(String vesselCode, double fuelThreshold);
}
