package com.example.maritimemetrics.controller;

import com.example.maritimemetrics.service.MaritimeMetricCsvService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the MaritimeMetricCsvController.
 * This class contains tests for the CSV import functionality of the controller.
 */
@SpringBootTest // Load the full application context
public class MaritimeMetricCsvControllerTest {

    @Autowired
    private MaritimeMetricCsvController maritimeMetricCsvController;

    private static MockMvc mockMvc;

    @Autowired
    private MaritimeMetricCsvService maritimeMetricCsvService;

    /**
     * Set up the MockMvc instance before each test.
     * This method initializes the MockMvc object for testing the controller.
     */
    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(maritimeMetricCsvController).build();
    }

    /**
     * Test the import of a CSV file.
     * This test verifies that a valid CSV file can be imported successfully
     * and the response status is OK (200).
     *
     * @throws Exception if an error occurs during the request
     */
    @Test
    public void testImportCsv() throws Exception {
        // Load the CSV file as a MockMultipartFile
        byte[] csvData = Files.readAllBytes(Paths.get("src/test/resources/data/vessel_data_test.csv"));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vessel_data_test.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvData
        );

        // Perform the multipart request to upload the file
        mockMvc.perform(multipart("/api/csv/import").file(file))
                .andExpect(status().isOk());
    }
}
