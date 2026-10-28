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

/** Unit tests for RegisterRequest. */
class RegisterRequestTest {

    @Test
    @DisplayName("a blank name, malformed email, and short password each violate a constraint")
    void a_blank_name_malformed_email_and_short_password_each_violate_a_constraint() throws Exception {
        // Arrange
        // three invalid fields
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RegisterRequest request = new RegisterRequest();
        request.setName(" ");
        request.setEmail("not-an-email");
        request.setPassword("123");

        // Act
        // validate
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Assert
        // exactly the three fields are flagged
        Set<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
        assertEquals(Set.of("name", "email", "password"), fields);
        assertTrue(violations.size() >= 3);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("a well-formed request has no violations")
    void a_well_formed_request_has_no_violations() throws Exception {
        // Arrange
        // valid values at the lower length bounds
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        RegisterRequest request = new RegisterRequest();
        request.setName("Al");
        request.setEmail("al@example.com");
        request.setPassword("123456");

        // Act
        // validate
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Assert
        // clean, and getters echo the values
        assertTrue(violations.isEmpty());
        assertEquals("Al", request.getName());
        assertEquals("al@example.com", request.getEmail());
        assertEquals("123456", request.getPassword());
    }
}
