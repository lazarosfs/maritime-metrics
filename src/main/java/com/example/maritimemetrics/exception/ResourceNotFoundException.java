package com.example.maritimemetrics.exception;

/**
 * Custom exception class to represent a resource not found scenario.
 * This exception is thrown when a requested resource cannot be found in the application.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
