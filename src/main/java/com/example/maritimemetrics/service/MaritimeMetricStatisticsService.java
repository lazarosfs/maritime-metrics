package com.example.maritimemetrics.service;

import com.example.maritimemetrics.exception.ResourceNotFoundException;
import com.example.maritimemetrics.model.MaritimeMetric;
import com.example.maritimemetrics.repository.MaritimeMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for performing statistical calculations and retrieving metrics related to maritime data.
 */
@Service
public class MaritimeMetricStatisticsService {

    private static final Logger LOGGER = Logger.getLogger(MaritimeMetricStatisticsService.class.getName());
    private static final Duration TIME_THRESHOLD = Duration.ofMinutes(5);  // Example: 5 minutes

    @Autowired
    private MaritimeMetricRepository maritimeMetricRepository;

    /**
     * Calculates the frequency of missing data for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return the count of missing data entries
     */
    public long calculateMissingFrequency(String vesselCode) {
        validateVesselExists(vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsMissingTrue(vesselCode).size();
    }

    /**
     * Calculates the frequency of below-zero data for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return the count of below-zero entries
     */
    public long calculateBelowZeroFrequency(String vesselCode) {
        validateVesselExists(vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsBelowZeroTrue(vesselCode).size();
    }

    /**
     * Calculates the frequency of outlier data for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return the count of outlier entries
     */
    public long calculateOutlierFrequency(String vesselCode) {
        validateVesselExists(vesselCode);
        return maritimeMetricRepository.findAllByVesselCodeAndIsOutlierTrue(vesselCode).size();
    }

    /**
     * Calculates validity metrics, including the total and invalid counts for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return a map containing total and invalid counts
     */
    public Map<String, Long> calculateValidityMetrics(String vesselCode) {
        validateVesselExists(vesselCode);
        Map<String, Long> validityMetrics = new HashMap<>();
        long total = maritimeMetricRepository.findAllByVesselCode(vesselCode).size();
        long invalid = maritimeMetricRepository.findAllByVesselCodeAndIsInvalidTrue(vesselCode).size();

        validityMetrics.put("Invalid", invalid);
        validityMetrics.put("Total", total);

        return validityMetrics;
    }

    /**
     * Retrieves problem frequencies and validity metrics for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return a map of problem frequencies and validity metrics
     */
    public Map<String, Long> getProblemFrequenciesAndValidityMetrics(String vesselCode) {
        validateVesselExists(vesselCode);
        Map<String, Long> metrics = new HashMap<>();

        metrics.put("Missing", calculateMissingFrequency(vesselCode));
        metrics.put("BelowZero", calculateBelowZeroFrequency(vesselCode));
        metrics.put("Outlier", calculateOutlierFrequency(vesselCode));

        Map<String, Long> validityMetrics = calculateValidityMetrics(vesselCode);
        metrics.putAll(validityMetrics);

        return metrics.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    /**
     * Calculates the median speed difference for a specific vessel.
     *
     * @param vesselCode the code of the vessel
     * @return the median speed difference
     */
    public double calculateMedianSpeedDifference(String vesselCode) {
        List<MaritimeMetric> waypoints = maritimeMetricRepository.findAllByVesselCode(vesselCode);

        List<Double> speedDifferences = waypoints.stream()
                .filter(m -> m.getActualSpeedOverground() != null && m.getProposedSpeedOverground() != null)
                .map(m -> Math.abs(m.getActualSpeedOverground() - m.getProposedSpeedOverground()))
                .sorted() // Sort the differences
                .toList();

        int size = speedDifferences.size();
        if (size == 0) {
            return 0; // Handle the case with no valid speed differences
        }

        if (size % 2 == 0) {
            // Average of the two middle numbers for even count
            return (speedDifferences.get(size / 2 - 1) + speedDifferences.get(size / 2)) / 2.0;
        } else {
            // Middle number for odd count
            return speedDifferences.get(size / 2);
        }
    }

    /**
     * Compares the median speed differences of two vessels.
     *
     * @param vesselCode1 the code of the first vessel
     * @param vesselCode2 the code of the second vessel
     * @return the vessel code of the vessel with the lower median speed difference, or "equal" if they are the same
     */
    public String compareVesselSpeedMedians(String vesselCode1, String vesselCode2) {
        double median1 = calculateMedianSpeedDifference(vesselCode1);
        double median2 = calculateMedianSpeedDifference(vesselCode2);

        if (median1 < median2) {
            return vesselCode1;
        } else if (median1 > median2) {
            return vesselCode2;
        } else {
            return "equal";
        }
    }

    /**
     * Retrieves speed outliers for a specific vessel based on a given threshold.
     *
     * @param vesselCode     the code of the vessel
     * @param speedThreshold the threshold for determining speed outliers
     * @return a list of MaritimeMetric objects that are considered speed outliers
     */
    public List<MaritimeMetric> getSpeedOutliers(String vesselCode, double speedThreshold) {
        validateVesselExists(vesselCode);
        validateThreshold(speedThreshold);

        List<MaritimeMetric> outliers = maritimeMetricRepository.findSpeedOutliersByVesselCode(vesselCode, speedThreshold);
        if (outliers.isEmpty()) {
            String message = "No speed outliers found for vessel " + vesselCode + " with threshold " + speedThreshold;
            LOGGER.log(Level.SEVERE, message);
            throw new ResourceNotFoundException(message);
        }
        return outliers;
    }

    /**
     * Retrieves fuel outliers for a specific vessel based on a given threshold.
     *
     * @param vesselCode    the code of the vessel
     * @param fuelThreshold the threshold for determining fuel outliers
     * @return a list of MaritimeMetric objects that are considered fuel outliers
     */
    public List<MaritimeMetric> getFuelOutliers(String vesselCode, double fuelThreshold) {
        validateVesselExists(vesselCode);
        validateThreshold(fuelThreshold);

        List<MaritimeMetric> outliers = maritimeMetricRepository.findFuelOutliersByVesselCode(vesselCode, fuelThreshold);
        if (outliers.isEmpty()) {
            String message = "No fuel outliers found for vessel " + vesselCode + " with threshold " + fuelThreshold;
            LOGGER.log(Level.SEVERE, message);
            throw new ResourceNotFoundException(message);
        }
        return outliers;
    }

    /**
     * Validates whether data exists for a specific vessel code.
     *
     * @param vesselCode the code of the vessel
     * @throws ResourceNotFoundException if no data is found for the vessel
     */
    private void validateVesselExists(String vesselCode) {
        if (maritimeMetricRepository.findAllByVesselCode(vesselCode).isEmpty()) {
            String message = "No data found for vessel " + vesselCode;
            LOGGER.log(Level.SEVERE, message);
            throw new ResourceNotFoundException(message);
        }
    }

    /**
     * Validates the threshold value.
     *
     * @param threshold the threshold value to validate
     * @throws IllegalArgumentException if the threshold is negative
     */
    private void validateThreshold(double threshold) {
        if (threshold < 0) {
            String message = "Threshold must be non-negative";
            LOGGER.log(Level.WARNING, message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Retrieves consecutive groups of problematic waypoints for a specific problem type.
     *
     * @param vesselCode  the code of the vessel
     * @param problemType the type of problem to filter by (e.g., "missing", "belowzero", "outlier")
     * @return a list of lists containing consecutive MaritimeMetric objects with the specified problem
     */
    public List<List<MaritimeMetric>> getConsecutiveProblematicGroups(String vesselCode, String problemType) {
        LOGGER.log(Level.INFO, "Retrieving consecutive groups for vessel: " + vesselCode + " with problem type: " + problemType);

        List<MaritimeMetric> waypoints = getWaypointsByProblemType(vesselCode, problemType);

        List<List<MaritimeMetric>> groupedProblems = new ArrayList<>();
        List<MaritimeMetric> currentGroup = new ArrayList<>();

        for (int i = 0; i < waypoints.size(); i++) {
            if (i == 0 || isConsecutive(waypoints.get(i - 1), waypoints.get(i))) {
                currentGroup.add(waypoints.get(i));
            } else {
                groupedProblems.add(new ArrayList<>(currentGroup));
                LOGGER.log(Level.INFO, "Added group with size: " + currentGroup.size());
                currentGroup.clear();
                currentGroup.add(waypoints.get(i));
            }
        }

        if (!currentGroup.isEmpty()) {
            groupedProblems.add(currentGroup);
            LOGGER.log(Level.INFO, "Added last group with size: " + currentGroup.size());
        }

        // Sort groups by size in descending order
        groupedProblems.sort((group1, group2) -> Integer.compare(group2.size(), group1.size()));
        LOGGER.log(Level.INFO, "Sorted groups by size in descending order for vessel: " + vesselCode);

        return groupedProblems;
    }

// Helper method to get waypoints by problem type

    /**
     * Retrieves waypoints for a specific vessel filtered by the type of problem.
     *
     * @param vesselCode  the code of the vessel
     * @param problemType the type of problem to filter by (e.g., "missing", "belowzero", "outlier")
     * @return a list of MaritimeMetric objects that match the specified problem type
     * @throws IllegalArgumentException if an unknown problem type is provided
     */
    private List<MaritimeMetric> getWaypointsByProblemType(String vesselCode, String problemType) {
        LOGGER.log(Level.INFO, "Fetching waypoints for vessel: " + vesselCode + " with problem type: " + problemType);

        switch (problemType.toLowerCase()) {
            case "missing":
                return maritimeMetricRepository.findAllByVesselCodeAndIsMissingTrue(vesselCode);
            case "belowzero":
                return maritimeMetricRepository.findAllByVesselCodeAndIsBelowZeroTrue(vesselCode);
            case "outlier":
                return maritimeMetricRepository.findAllByVesselCodeAndIsOutlierTrue(vesselCode);
            default:
                String errorMsg = "Unknown problem type: " + problemType;
                LOGGER.log(Level.SEVERE, errorMsg);
                throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Checks if two waypoints are consecutive based on their timestamps.
     *
     * @param prev    the previous MaritimeMetric object
     * @param current the current MaritimeMetric object
     * @return true if the waypoints are consecutive, false otherwise
     */
    private boolean isConsecutive(MaritimeMetric prev, MaritimeMetric current) {
        if (prev.getDatetime() == null || current.getDatetime() == null) {
            LOGGER.log(Level.WARNING, "One of the waypoints has a null timestamp, marking as non-consecutive");
            return false;
        }

        Duration timeDifference = Duration.between(prev.getDatetime(), current.getDatetime());
        boolean isConsecutive = !timeDifference.isNegative() && timeDifference.compareTo(TIME_THRESHOLD) <= 0;

        LOGGER.log(Level.INFO, "Checking consecutiveness: Previous timestamp = " + prev.getDatetime() +
                ", Current timestamp = " + current.getDatetime() + ", isConsecutive = " + isConsecutive);

        return isConsecutive;
    }
}
