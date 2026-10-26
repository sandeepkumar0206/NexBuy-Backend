package com.nexbuy.dto.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;

/** Day 2 | AC: LoginRequest DTO | BR: email format, password required */
class LoginRequestTest {

    // HIGH
    @Test
    @DisplayName("a malformed email and blank password violate constraints")
    void a_malformed_email_and_blank_password_violate_constraints() throws Exception {
        // Arrange
        // two invalid fields
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        LoginRequest request = new LoginRequest();
        request.setEmail("nope");
        request.setPassword("");

        // Act
        // validate
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        // both fields flagged
        Set<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
        assertEquals(Set.of("email", "password"), fields);
        assertEquals(2, violations.size());
        assertFalse(violations.isEmpty());
    }
}
