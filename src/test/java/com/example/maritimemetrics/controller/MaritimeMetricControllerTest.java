package com.example.maritimemetrics.controller;

import com.example.maritimemetrics.MaritimeMetricsApplication;
import com.example.maritimemetrics.service.MaritimeMetricCsvService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the MaritimeMetricController.
 * This class contains tests for various endpoints of the MaritimeMetricController
 * and ensures that they return the expected results based on the provided CSV data.
 */
@SpringBootTest(classes = MaritimeMetricsApplication.class)
@AutoConfigureMockMvc
public class MaritimeMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaritimeMetricCsvService maritimeMetricCsvService;

    /**
     * Load data from CSV before all tests.
     *
     * @param csvService The CSV service used to import data.
     * @throws Exception if there is an issue loading the data from the CSV file.
     */
    @BeforeAll
    public static void setUpOnce(@Autowired MaritimeMetricCsvService csvService) throws Exception {
        loadDataFromCsvUsingService(csvService);
    }

    /**
     * Loads data from the specified CSV file into the database using the provided CSV service.
     *
     * @param csvService The CSV service used to import data.
     * @throws Exception if there is an issue reading the CSV file.
     */
    private static void loadDataFromCsvUsingService(MaritimeMetricCsvService csvService) throws Exception {
        byte[] csvData = Files.readAllBytes(Paths.get("src/test/resources/data/vessel_data_test.csv"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vessel_data_test.csv",
                "text/csv",
                csvData
        );
        csvService.importDataFromCsv(file);
    }

    /**
     * Test for fetching all data from the metrics API.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllData() throws Exception {
        mockMvc.perform(get("/api/metrics/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(13)));  // Adjust based on the number of entries in your CSV
    }

    /**
     * Test for fetching all vessel codes from the metrics API.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllVesselCodes() throws Exception {
        mockMvc.perform(get("/api/metrics/vessel-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Based on your CSV, should return unique vessel codes
                .andExpect(jsonPath("$[0]", is("19310")))
                .andExpect(jsonPath("$[1]", is("3001")));
    }

    /**
     * Test for fetching all valid data for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllValidDataByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/3001/valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vesselCode", is("3001")));  // Valid data should be loaded from CSV
    }

    /**
     * Test for fetching all invalid data for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllInvalidDataByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/19310/invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Adjust based on your test data
    }

    /**
     * Test for fetching all missing data for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllMissingDataByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/19310/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Adjust based on your test data
    }

    /**
     * Test for fetching all below-zero data for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllBelowZeroDataByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/3001/below-zero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))); // Adjust based on your test data
    }

    /**
     * Test for fetching all outlier data for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetAllOutlierDataByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/3001/outlier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))); // Adjust based on your test data
    }

    /**
     * Test for fetching speed differences for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetSpeedDifferencesByVesselCode() throws Exception {
        mockMvc.perform(get("/api/metrics/3001/speed-difference"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].latitude", is(10.2894496917725)))  // Expected value from CSV
                .andExpect(jsonPath("$[0].longitude", is(-14.7888498306274)))  // Expected value from CSV
                .andExpect(jsonPath("$[0].speedDifference", is(-1.0E-5)));  // Example expected value calculated from your logic
    }

    /**
     * Test for fetching problem frequencies for a specific vessel code.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetProblemFrequenciesSorted() throws Exception {
        mockMvc.perform(get("/api/metrics/19310/problems/frequencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BelowZero", is(1)))
                .andExpect(jsonPath("$.Missing", is(1)))  // Adjust based on actual data
                .andExpect(jsonPath("$.Outlier", is(2)));  // Adjust based on actual data
    }

    /**
     * Test for fetching metrics for a specific vessel within a specified time period.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testGetMetricsForVesselInPeriod() throws Exception {
        mockMvc.perform(get("/api/metrics/3001/summary-metrics")
                        .param("startDate", "2023-06-01T00:01:00") // Changed to use 'T'
                        .param("endDate", "2023-06-01T00:03:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].power", is(0.0)))  // Adjust based on your CSV data
                .andExpect(jsonPath("$[0].fuelConsumption", is(0.0)))  // Adjust based on your CSV data
                .andExpect(jsonPath("$[0].actualSpeedOverground", is(0.09999)))  // Example expected value
                .andExpect(jsonPath("$[0].proposedSpeedOverground", is(0.1)))  // Example expected value
                .andExpect(jsonPath("$[0].predictedFuelConsumption", is(0.0)))  // Example expected value
                .andExpect(jsonPath("$[0].speedDifference", is(-1.0E-5)));  // Example expected value
    }

    /**
     * Test for comparing the compliance of two vessels based on their median speed differences.
     *
     * @throws Exception if the test fails.
     */
    @Test
    public void testCompareVesselComplianceByAverageSpeedDifference() throws Exception {
        mockMvc.perform(get("/api/metrics/more-compliant")
                        .param("vesselCode1", "3001")
                        .param("vesselCode2", "19310"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", is("3001"))); // Expected result based on your test data
    }
}
