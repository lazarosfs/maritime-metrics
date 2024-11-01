package com.example.maritimemetrics.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for error responses.
 */
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private String details;

    /**
     * Constructs an ErrorResponse with the specified timestamp, message, and details.
     *
     * @param timestamp the timestamp of when the error occurred
     * @param message a brief description of the error
     * @param details additional details about the error
     */
    public ErrorResponse(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    // Getters and setters can be added here if needed
}
