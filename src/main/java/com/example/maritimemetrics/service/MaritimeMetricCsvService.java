package com.example.maritimemetrics.service;

import com.example.maritimemetrics.model.MaritimeMetric;
import com.example.maritimemetrics.repository.MaritimeMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for handling CSV import and processing of maritime metrics.
 * Parses and validates data from a CSV file and saves valid entries to the database.
 */
@Service
public class MaritimeMetricCsvService {

    private static final Logger LOGGER = Logger.getLogger(MaritimeMetricCsvService.class.getName());

    @Autowired
    private MaritimeMetricRepository maritimeMetricRepository;

    private static final double SPEED_PERCENTAGE_THRESHOLD = 0.5;  // 50% for speed
    private static final double FUEL_PERCENTAGE_THRESHOLD = 0.5;   // 50% for fuel
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Imports data from a CSV file and stores it in the database after validation.
     *
     * @param file the CSV file to import
     */
    public void importDataFromCsv(MultipartFile file) {
        maritimeMetricRepository.deleteAll();
        List<MaritimeMetric> metrics = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] fields = line.split(",");
                MaritimeMetric metric = parseAndValidate(fields);

                if (metric != null) {
                    metrics.add(metric);
                }
            }
            maritimeMetricRepository.saveAll(metrics);
            LOGGER.log(Level.INFO, "Successfully imported data from CSV with " + metrics.size() + " records.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading CSV file: " + e.getMessage(), e);
            throw new IllegalArgumentException("Failed to process CSV file.");
        }
    }

    /**
     * Parses and validates a row of CSV data.
     *
     * @param fields array of strings representing the columns in a CSV row
     * @return a MaritimeMetric object if validation passes; null otherwise
     */
    private MaritimeMetric parseAndValidate(String[] fields) {
        MaritimeMetric metric = new MaritimeMetric();

        if (!validateVesselCode(removeQuotes(fields[0]), metric)) {
            LOGGER.log(Level.WARNING, "Skipping row with invalid vessel code");
            return null;
        }

        validateDatetime(removeQuotes(fields[1]), metric);
        validateLatitude(removeQuotes(fields[2]), metric);
        validateLongitude(removeQuotes(fields[3]), metric);
        validatePower(removeQuotes(fields[4]), metric);
        validateFuelConsumption(removeQuotes(fields[5]), metric);
        validateActualSpeed(removeQuotes(fields[6]), metric);
        validateProposedSpeed(removeQuotes(fields[7]), metric);
        validatePredictedFuelConsumption(removeQuotes(fields[8]), metric);

        // Check for below-zero conditions after validating all fields
        checkBelowZeroCondition(metric);
        calculateDerivedFields(metric);
        return metric;
    }

    /**
     * Checks and sets the below-zero condition flag for specified metric fields.
     *
     * @param metric the MaritimeMetric object to update
     */
    private void checkBelowZeroCondition(MaritimeMetric metric) {
        boolean belowZero = metric.getPower() != null && metric.getPower() < 0;

        if (metric.getFuelConsumption() != null && metric.getFuelConsumption() < 0) {
            belowZero = true;
        }
        if (metric.getActualSpeedOverground() != null && metric.getActualSpeedOverground() < 0) {
            belowZero = true;
        }
        if (metric.getProposedSpeedOverground() != null && metric.getProposedSpeedOverground() < 0) {
            belowZero = true;
        }
        if (metric.getPredictedFuelConsumption() != null && metric.getPredictedFuelConsumption() < 0) {
            belowZero = true;
        }

        metric.setIsBelowZero(belowZero);
        if (belowZero) {
            LOGGER.log(Level.INFO, "Set IsBelowZero flag to true for vessel " + metric.getVesselCode());
        }
    }

    /**
     * Removes surrounding quotes from a string, if present.
     *
     * @param value the string to process
     * @return the unquoted string
     */
    private String removeQuotes(String value) {
        return value.replaceAll("^\"|\"$", "").trim();
    }

    /**
     * Validates and sets the vessel code.
     *
     * @param vesselCode the vessel code to validate
     * @param metric     the MaritimeMetric object to update
     * @return true if valid; false otherwise
     */
    private boolean validateVesselCode(String vesselCode, MaritimeMetric metric) {
        if (vesselCode == null || vesselCode.isEmpty()) {
            LOGGER.log(Level.WARNING, "Invalid vessel code: " + vesselCode);
            return false;
        }
        metric.setVesselCode(vesselCode);
        return true;
    }

    /**
     * Validates and sets the datetime.
     *
     * @param datetimeStr the datetime string to validate
     * @param metric      the MaritimeMetric object to update
     */
    private void validateDatetime(String datetimeStr, MaritimeMetric metric) {
        try {
            metric.setDatetime(LocalDateTime.parse(datetimeStr, DATE_TIME_FORMATTER));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid datetime format for value: " + datetimeStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the latitude.
     *
     * @param latitudeStr the latitude string to validate
     * @param metric      the MaritimeMetric object to update
     */
    private void validateLatitude(String latitudeStr, MaritimeMetric metric) {
        try {
            if (latitudeStr == null || latitudeStr.equalsIgnoreCase("NULL") || latitudeStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Latitude value is missing: " + latitudeStr);
                metric.setIsMissing(true);
                return;
            }

            Double latitude = Double.parseDouble(latitudeStr);
            if (latitude >= -90 && latitude <= 90) {
                metric.setLatitude(latitude);
            } else {
                LOGGER.log(Level.WARNING, "Latitude out of range for value: " + latitudeStr);
                metric.setIsOutlier(true);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid latitude format for value: " + latitudeStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the longitude.
     *
     * @param longitudeStr the longitude string to validate
     * @param metric       the MaritimeMetric object to update
     */
    private void validateLongitude(String longitudeStr, MaritimeMetric metric) {
        try {
            if (longitudeStr == null || longitudeStr.equalsIgnoreCase("NULL") || longitudeStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Longitude value is missing: " + longitudeStr);
                metric.setIsMissing(true);
                return;
            }

            Double longitude = Double.parseDouble(longitudeStr);
            if (longitude >= -180 && longitude <= 180) {
                metric.setLongitude(longitude);
            } else {
                LOGGER.log(Level.WARNING, "Longitude out of range for value: " + longitudeStr);
                metric.setIsOutlier(true);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid longitude format for value: " + longitudeStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the power value from the provided string.
     * If the value is null, empty, or "NULL", it marks the metric as missing.
     *
     * @param powerStr the power string to validate
     * @param metric   the MaritimeMetric object to update
     */
    private void validatePower(String powerStr, MaritimeMetric metric) {
        try {
            // Check if the value is "NULL" or empty, and set as missing if true
            if (powerStr == null || powerStr.equalsIgnoreCase("NULL") || powerStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Power value is missing: " + powerStr);
                metric.setIsMissing(true);
                return;
            }

            // Proceed with parsing if value is not "NULL"
            Double power = Double.parseDouble(powerStr);
            metric.setPower(power);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid power format for value: " + powerStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the fuel consumption value from the provided string.
     * If the value is null, empty, or "NULL", it marks the metric as missing.
     *
     * @param fuelConsumptionStr the fuel consumption string to validate
     * @param metric             the MaritimeMetric object to update
     */
    private void validateFuelConsumption(String fuelConsumptionStr, MaritimeMetric metric) {
        try {
            // Check if the value is "NULL" or empty, and set as missing if true
            if (fuelConsumptionStr == null || fuelConsumptionStr.equalsIgnoreCase("NULL") || fuelConsumptionStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Fuel consumption value is missing: " + fuelConsumptionStr);
                metric.setIsMissing(true);
                return;
            }

            // Proceed with parsing if value is not "NULL"
            metric.setFuelConsumption(Double.parseDouble(fuelConsumptionStr));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid fuel consumption format for value: " + fuelConsumptionStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the actual speed value from the provided string.
     * If the value is null, empty, or "NULL", it marks the metric as missing.
     *
     * @param actualSpeedStr the actual speed string to validate
     * @param metric         the MaritimeMetric object to update
     */
    private void validateActualSpeed(String actualSpeedStr, MaritimeMetric metric) {
        try {
            // Check if the value is "NULL" or empty, and set as missing if true
            if (actualSpeedStr == null || actualSpeedStr.equalsIgnoreCase("NULL") || actualSpeedStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Actual speed value is missing: " + actualSpeedStr);
                metric.setIsMissing(true);
                return;
            }

            // Proceed with parsing if value is not "NULL"
            metric.setActualSpeedOverground(Double.parseDouble(actualSpeedStr));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid actual speed format for value: " + actualSpeedStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the proposed speed value from the provided string.
     * If the value is null, empty, or "NULL", it marks the metric as missing.
     * It also checks for outliers based on the proposed and actual speeds.
     *
     * @param proposedSpeedStr the proposed speed string to validate
     * @param metric           the MaritimeMetric object to update
     */
    private void validateProposedSpeed(String proposedSpeedStr, MaritimeMetric metric) {
        try {
            // Check if the value is "NULL" or empty, and set as missing if true
            if (proposedSpeedStr == null || proposedSpeedStr.equalsIgnoreCase("NULL") || proposedSpeedStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Proposed speed value is missing: " + proposedSpeedStr);
                metric.setIsMissing(true);
                return;
            }

            // Proceed with parsing if value is not "NULL"
            Double proposedSpeed = Double.parseDouble(proposedSpeedStr);
            metric.setProposedSpeedOverground(proposedSpeed);

            // Check for outlier based on proposed and actual speeds
            if (metric.getActualSpeedOverground() != null &&
                    Math.abs((metric.getActualSpeedOverground() - proposedSpeed) / metric.getActualSpeedOverground()) > SPEED_PERCENTAGE_THRESHOLD) {
                LOGGER.log(Level.INFO, "Proposed speed outlier detected for actual: " + metric.getActualSpeedOverground() +
                        " and proposed: " + proposedSpeed);
                metric.setIsOutlier(true);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid proposed speed format for value: " + proposedSpeedStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Validates and sets the predicted fuel consumption value from the provided string.
     * If the value is null, empty, or "NULL", it marks the metric as missing.
     * It also checks for outliers based on the predicted and actual fuel consumption.
     *
     * @param predictedFuelStr the predicted fuel consumption string to validate
     * @param metric           the MaritimeMetric object to update
     */
    private void validatePredictedFuelConsumption(String predictedFuelStr, MaritimeMetric metric) {
        try {
            // Check if the value is "NULL" or empty, and set as missing if true
            if (predictedFuelStr == null || predictedFuelStr.equalsIgnoreCase("NULL") || predictedFuelStr.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Predicted fuel consumption value is missing: " + predictedFuelStr);
                metric.setIsMissing(true);
                return;
            }

            // Proceed with parsing if value is not "NULL"
            Double predictedFuel = Double.parseDouble(predictedFuelStr);
            metric.setPredictedFuelConsumption(predictedFuel);

            // Check for outlier based on predicted and actual fuel consumption
            if (metric.getFuelConsumption() != null &&
                    Math.abs((metric.getFuelConsumption() - predictedFuel) / metric.getFuelConsumption()) > FUEL_PERCENTAGE_THRESHOLD) {
                LOGGER.log(Level.INFO, "Fuel consumption outlier detected for actual: " + metric.getFuelConsumption() +
                        " and predicted: " + predictedFuel);
                metric.setIsOutlier(true);
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid predicted fuel consumption format for value: " + predictedFuelStr, e);
            metric.setIsMissing(true);
        }
    }

    /**
     * Calculates and sets derived fields for the given MaritimeMetric object.
     * Specifically, it calculates the speed difference between actual and proposed speeds,
     * rounds it to six decimal places, and sets the speedDifference property.
     * It also determines if the metric is invalid based on below-zero, missing, or outlier flags.
     *
     * @param metric the MaritimeMetric object for which to calculate derived fields
     */
    private void calculateDerivedFields(MaritimeMetric metric) {
        if (metric.getActualSpeedOverground() != null && metric.getProposedSpeedOverground() != null) {
            double speedDifference = metric.getActualSpeedOverground() - metric.getProposedSpeedOverground();

            // If using BigDecimal for rounding, convert back to double
            BigDecimal roundedSpeedDifference = new BigDecimal(speedDifference).setScale(6, RoundingMode.HALF_UP);

            // Ensure you're setting a double value back
            metric.setSpeedDifference(roundedSpeedDifference.doubleValue());

            LOGGER.log(Level.INFO, "Calculated speed difference for vessel " + metric.getVesselCode() +
                    ": " + metric.getSpeedDifference());
        }

        metric.setIsInvalid(
                Boolean.TRUE.equals(metric.getIsBelowZero()) ||
                        Boolean.TRUE.equals(metric.getIsMissing()) ||
                        Boolean.TRUE.equals(metric.getIsOutlier())
        );

        LOGGER.log(Level.INFO, "Set IsInvalid flag to " + metric.getIsInvalid() + " for vessel " + metric.getVesselCode());
    }
}
