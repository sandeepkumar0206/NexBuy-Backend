package com.nexbuy.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;

/** Unit tests for ApiException. */
class ApiExceptionTest {

    @Test
    @DisplayName("the single-argument constructor defaults to 400")
    void the_single_argument_constructor_defaults_to_400() throws Exception {
        // Arrange
        // message only
        String message = "Something is wrong";

        // Act
        // construct
        ApiException error = new ApiException(message);

        // Assert
        // default status and runtime type
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("Something is wrong", error.getMessage());
        assertTrue(error instanceof RuntimeException);
    }

    @Test
    @DisplayName("the two-argument constructor keeps the given status")
    void the_two_argument_constructor_keeps_the_given_status() throws Exception {
        // Arrange
        // message and explicit status
        String message = "Gone for good";

        // Act
        // construct
        ApiException error = new ApiException(message, HttpStatus.GONE);

        // Assert
        // status kept, no cause chained
        assertEquals(HttpStatus.GONE, error.getStatus());
        assertEquals("Gone for good", error.getMessage());
        assertNull(error.getCause());
    }
}
