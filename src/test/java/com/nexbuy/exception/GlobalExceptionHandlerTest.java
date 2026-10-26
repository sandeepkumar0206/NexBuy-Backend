package com.nexbuy.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.exception.ApiException;
import com.nexbuy.exception.GlobalExceptionHandler;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Day 3 | AC: global exception handler returns 400/401/404 | BR: JSON body with status + message, no internals leaked */
class GlobalExceptionHandlerTest {

    // HIGH
    @Test
    @DisplayName("handleApiException responds with the exception's own status and message")
    void handleapiexception_responds_with_the_exception_s_own_status_and_message() throws Exception {
        // Arrange
        // a 404 ApiException
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiException error = new ApiException("Product not found", HttpStatus.NOT_FOUND);

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleApiException(error);

        // Assert
        // status line and JSON body agree
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Product not found", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    // HIGH
    @Test
    @DisplayName("handleNotFound maps a missing resource to 404")
    void handlenotfound_maps_a_missing_resource_to_404() throws Exception {
        // Arrange
        // unknown path
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        NoResourceFoundException error = new NoResourceFoundException(HttpMethod.GET, "api/nothing");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(error);

        // Assert
        // generic message, no path leaked
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().get("message"));
        assertEquals(404, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleForbidden maps AccessDeniedException to 403")
    void handleforbidden_maps_accessdeniedexception_to_403() throws Exception {
        // Arrange
        // denied access
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AccessDeniedException error = new AccessDeniedException("internal detail");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleForbidden(error);

        // Assert
        // friendly message, internal detail hidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to access this resource", response.getBody().get("message"));
        assertEquals(403, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleAuth maps bad credentials to 401 with a generic message")
    void handleauth_maps_bad_credentials_to_401_with_a_generic_message() throws Exception {
        // Arrange
        // authentication failure
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BadCredentialsException error = new BadCredentialsException("password mismatch for sam");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleAuth(error);

        // Assert
        // does not reveal which part was wrong
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("message"));
        assertEquals(401, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleValidation returns every field error in an errors map")
    void handlevalidation_returns_every_field_error_in_an_errors_map() throws Exception {
        // Arrange
        // two field errors on a binding result
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "registerRequest");
        binding.addError(new FieldError("registerRequest", "email", "must be a well-formed email address"));
        binding.addError(new FieldError("registerRequest", "password", "size must be between 6 and 100"));
        MethodParameter parameter = new MethodParameter(Object.class.getDeclaredMethod("toString"), -1);
        MethodArgumentNotValidException error = new MethodArgumentNotValidException(parameter, binding);
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleValidation(error);

        // Assert
        // 400 with per-field messages
        Map<?, ?> fields = (Map<?, ?>) response.getBody().get("errors");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().get("message"));
        assertEquals("must be a well-formed email address", fields.get("email"));
        assertEquals("size must be between 6 and 100", fields.get("password"));
    }

    // HIGH
    @Test
    @DisplayName("handleTypeMismatch names the offending parameter")
    void handletypemismatch_names_the_offending_parameter() throws Exception {
        // Arrange
        // "abc" supplied for a Long id
        MethodParameter parameter = new MethodParameter(Object.class.getDeclaredMethod("toString"), -1);
        MethodArgumentTypeMismatchException error = new MethodArgumentTypeMismatchException("abc", Long.class, "id", parameter, new NumberFormatException("abc"));
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(error);

        // Assert
        // 400 naming the parameter, not the raw value
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for parameter 'id'", response.getBody().get("message"));
        assertEquals(400, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleMissingParam names the missing parameter")
    void handlemissingparam_names_the_missing_parameter() throws Exception {
        // Arrange
        // q is required but absent
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MissingServletRequestParameterException error = new MissingServletRequestParameterException("q", "String");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleMissingParam(error);

        // Assert
        // 400 with the parameter name
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required parameter 'q'", response.getBody().get("message"));
        assertEquals(400, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleUnreadableBody hides parser details behind a generic 400")
    void handleunreadablebody_hides_parser_details_behind_a_generic_400() throws Exception {
        // Arrange
        // JSON parse failure with internal detail
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpMessageNotReadableException error = new HttpMessageNotReadableException("Unexpected token at line 1", mock(HttpInputMessage.class));

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleUnreadableBody(error);

        // Assert
        // generic message only
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed request body", response.getBody().get("message"));
        assertFalse(String.valueOf(response.getBody().get("message")).contains("Unexpected token"));
    }

    // HIGH
    @Test
    @DisplayName("handleMethodNotAllowed returns 405 naming the method")
    void handlemethodnotallowed_returns_405_naming_the_method() throws Exception {
        // Arrange
        // DELETE on a read-only endpoint
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpRequestMethodNotSupportedException error = new HttpRequestMethodNotSupportedException("DELETE");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleMethodNotAllowed(error);

        // Assert
        // 405 and message
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Method DELETE is not supported for this endpoint", response.getBody().get("message"));
        assertEquals(405, response.getBody().get("status"));
    }

    // HIGH
    @Test
    @DisplayName("handleGeneric returns 500 without leaking the exception message")
    void handlegeneric_returns_500_without_leaking_the_exception_message() throws Exception {
        // Arrange
        // unexpected failure with sensitive text
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalStateException error = new IllegalStateException("db password is hunter2");

        // Act
        // handle it
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(error);

        // Assert
        // generic text only
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong. Please try again later.", response.getBody().get("message"));
        assertFalse(String.valueOf(response.getBody().get("message")).contains("hunter2"));
        assertEquals(500, response.getBody().get("status"));
    }
}
