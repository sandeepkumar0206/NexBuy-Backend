package com.nexbuy.dto.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.*;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for RefreshRequest. */
class RefreshRequestTest {

    @Test
    @DisplayName("a blank refresh token violates the not-blank constraint")
    void a_blank_refresh_token_violates_the_not_blank_constraint() throws Exception {
        // Arrange
        // empty token
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("");

        // Act
        // validate
        Set<ConstraintViolation<RefreshRequest>> violations = validator.validate(request);

        // Assert
        // one violation on refreshToken
        assertEquals(1, violations.size());
        assertEquals("refreshToken", violations.iterator().next().getPropertyPath().toString());
        assertEquals("", request.getRefreshToken());
    }
}
