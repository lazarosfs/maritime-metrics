package com.example.maritimemetrics.controller;

import com.example.maritimemetrics.service.MaritimeMetricCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for handling CSV import operations for maritime metrics.
 */
@RestController
@RequestMapping("/api/csv")
public class MaritimeMetricCsvController {

    private static final Logger LOGGER = Logger.getLogger(MaritimeMetricCsvController.class.getName());

    @Autowired
    private MaritimeMetricCsvService maritimeMetricCsvService;

    /**
     * Imports a CSV file containing maritime metrics data.
     *
     * @param file the CSV file to be imported
     * @return a ResponseEntity containing the result of the import operation
     */
    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        LOGGER.log(Level.INFO, "CSV import request received");

        if (file.isEmpty()) {
            LOGGER.log(Level.WARNING, "CSV import failed: No file provided");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file provided. Please upload a valid CSV file.");
        }

        try {
            maritimeMetricCsvService.importDataFromCsv(file);
            LOGGER.log(Level.INFO, "CSV file imported successfully");
            return ResponseEntity.ok("CSV file imported successfully");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "CSV import failed due to invalid data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid data in CSV file: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during CSV import: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred during CSV import.");
        }
    }
}
