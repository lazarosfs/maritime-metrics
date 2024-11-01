package com.example.maritimemetrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Maritime Metrics application.
 * This class contains the main method which is used to launch the Spring Boot application.
 */
@SpringBootApplication
public class MaritimeMetricsApplication {

	/**
	 * Main method that serves as the entry point for the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(MaritimeMetricsApplication.class, args);
	}
}
