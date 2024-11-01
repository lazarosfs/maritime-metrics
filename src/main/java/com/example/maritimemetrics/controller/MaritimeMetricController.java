package com.example.maritimemetrics.controller;

import com.example.maritimemetrics.dto.MetricSummaryDTO;
import com.example.maritimemetrics.dto.SpeedDifferenceDTO;
import com.example.maritimemetrics.model.MaritimeMetric;
import com.example.maritimemetrics.repository.MaritimeMetricRepository;
import com.example.maritimemetrics.service.MaritimeMetricStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST controller providing endpoints to manage and retrieve maritime metrics.
 */
@RestController
@RequestMapping("/api/metrics")
public class MaritimeMetricController {

    private static final Logger LOGGER = Logger.getLogger(MaritimeMetricController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MaritimeMetricRepository maritimeMetricRepository;

    @Autowired
    private MaritimeMetricStatisticsService maritimeMetricStatisticsService;

    /**
     * Retrieves all maritime metrics data.
     *
     * @return a list of all maritime metrics
     */
    @GetMapping("/all")
    public List<MaritimeMetric> getAllData() {
        LOGGER.log(Level.INFO, "Fetching all metrics data");
        return maritimeMetricRepository.findAll();
    }

    /**
     * Retrieves all unique vessel codes.
     *
     * @return a list of vessel codes
     */
    @GetMapping("/vessel-codes")
    public List<String> getAllVesselCodes() {
        LOGGER.log(Level.INFO, "Fetching all vessel codes");
        return maritimeMetricRepository.findAllVesselCodes();
    }

    /**
     * Retrieves valid data for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of valid maritime metrics for the specified vessel
     */
    @GetMapping("/{vesselCode}/valid")
    public List<MaritimeMetric> getAllValidDataByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching all valid data for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsInvalidFalse(vesselCode);
    }

    /**
     * Retrieves invalid data for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of invalid maritime metrics for the specified vessel
     */
    @GetMapping("/{vesselCode}/invalid")
    public List<MaritimeMetric> getAllInvalidDataByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching all invalid data for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsInvalidTrue(vesselCode);
    }

    /**
     * Retrieves data with missing values for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of maritime metrics with missing values for the specified vessel
     */
    @GetMapping("/{vesselCode}/missing")
    public List<MaritimeMetric> getAllMissingDataByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching all missing data for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsMissingTrue(vesselCode);
    }

    /**
     * Retrieves below-zero data for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of maritime metrics with below-zero values for the specified vessel
     */
    @GetMapping("/{vesselCode}/below-zero")
    public List<MaritimeMetric> getAllBelowZeroDataByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching all below-zero data for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsBelowZeroTrue(vesselCode);
    }

    /**
     * Retrieves outlier data for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of maritime metrics with outlier values for the specified vessel
     */
    @GetMapping("/{vesselCode}/outlier")
    public List<MaritimeMetric> getAllOutlierDataByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching all outlier data for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsOutlierTrue(vesselCode);
    }

    /**
     * Retrieves speed difference data for a specific vessel.
     *
     * @param vesselCode the vessel code
     * @return a list of speed differences for the specified vessel
     */
    @GetMapping("/{vesselCode}/speed-difference")
    public List<SpeedDifferenceDTO> getSpeedDifferencesByVesselCode(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching speed differences for vesselCode: " + vesselCode);
        return maritimeMetricRepository.findSpeedDifferencesByVesselCode(vesselCode);
    }

    /**
     * Retrieves problem frequencies for a specific vessel, sorted by frequency.
     *
     * @param vesselCode the vessel code
     * @return a map of problem types and their frequencies for the specified vessel
     */
    @GetMapping("/{vesselCode}/problems/frequencies")
    public Map<String, Long> getProblemFrequenciesSorted(@PathVariable String vesselCode) {
        LOGGER.log(Level.INFO, "Fetching problem frequencies for vesselCode: " + vesselCode);
        return maritimeMetricStatisticsService.getProblemFrequenciesAndValidityMetrics(vesselCode);
    }

    /**
     * Retrieves grouped problematic waypoints for a specified problem type in a vessel.
     *
     * @param vesselCode the vessel code
     * @param problemType the type of problem (e.g., "missing", "below-zero", "outlier")
     * @return a list of grouped waypoints with the specified problem type
     */
    @GetMapping("/{vesselCode}/problems/groups")
    public List<List<MaritimeMetric>> getProblematicGroups(
            @PathVariable String vesselCode,
            @RequestParam String problemType) {
        LOGGER.log(Level.INFO, "Fetching problematic groups for vesselCode: " + vesselCode + " with problemType: " + problemType);
        return maritimeMetricStatisticsService.getConsecutiveProblematicGroups(vesselCode, problemType);
    }

    /**
     * Retrieves metrics for a specific vessel within a specified time period.
     *
     * @param vesselCode the vessel code
     * @param startDate the start date in "yyyy-MM-dd'T'HH:mm:ss" format
     * @param endDate the end date in "yyyy-MM-dd'T'HH:mm:ss" format
     * @return a list of metric summaries within the specified period
     */
    @GetMapping("/{vesselCode}/summary-metrics")
    public List<MetricSummaryDTO> getMetricsForVesselInPeriod(
            @PathVariable String vesselCode,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LOGGER.log(Level.INFO, "Fetching summary metrics for vesselCode: " + vesselCode + " between " + startDate + " and " + endDate);
        LocalDateTime start = LocalDateTime.parse(startDate, DATE_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endDate, DATE_FORMATTER);
        return maritimeMetricRepository.findMetricsByVesselCodeAndDatetimeBetween(vesselCode, start, end);
    }

    /**
     * Compares two vessels based on median speed difference to determine which is more compliant.
     *
     * @param vesselCode1 the first vessel code
     * @param vesselCode2 the second vessel code
     * @return a JSON object with the vessel code that has higher compliance
     */
    @GetMapping("/more-compliant")
    public ResponseEntity<Map<String, String>> getMedianSpeedDifference(@RequestParam String vesselCode1, @RequestParam String vesselCode2) {
        String result = maritimeMetricStatisticsService.compareVesselSpeedMedians(vesselCode1, vesselCode2);
        Map<String, String> response = new HashMap<>();
        response.put("result", result);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves speed outliers for a specific vessel based on a threshold.
     *
     * @param vesselCode the vessel code
     * @param speedThreshold the speed threshold for outliers
     * @return a list of speed outliers
     */
    @GetMapping("/outliers/speed")
    public List<MaritimeMetric> getSpeedOutliers(
            @RequestParam String vesselCode,
            @RequestParam double speedThreshold) {
        LOGGER.log(Level.INFO, "Fetching speed outliers for vesselCode: " + vesselCode + " with speedThreshold: " + speedThreshold);
        return maritimeMetricStatisticsService.getSpeedOutliers(vesselCode, speedThreshold);
    }

    /**
     * Retrieves fuel outliers for a specific vessel based on a threshold.
     *
     * @param vesselCode the vessel code
     * @param fuelThreshold the fuel threshold for outliers
     * @return a list of fuel outliers
     */
    @GetMapping("/outliers/fuel")
    public List<MaritimeMetric> getFuelOutliers(
            @RequestParam String vesselCode,
            @RequestParam double fuelThreshold) {
        LOGGER.log(Level.INFO, "Fetching fuel outliers for vesselCode: " + vesselCode + " with fuelThreshold: " + fuelThreshold);
        return maritimeMetricStatisticsService.getFuelOutliers(vesselCode, fuelThreshold);
    }
}


