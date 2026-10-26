package com.nexbuy.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.auth.*;
import com.nexbuy.service.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Day 2 | AC: POST /api/auth/register, /login, /refresh | BR: register answers 201 */
class AuthControllerTest {

    // MEDIUM
    @Test
    @DisplayName("register delegates to the service and is annotated to answer 201")
    void register_delegates_to_the_service_and_is_annotated_to_answer_201() throws Exception {
        // Arrange
        // service returns a canned response
        AuthService service = mock(AuthService.class);
        RegisterRequest request = new RegisterRequest();
        AuthResponse expected = AuthResponse.builder().accessToken("a").build();
        when(service.register(request)).thenReturn(expected);
        AuthController controller = new AuthController(service);

        // Act
        // call the endpoint method
        AuthResponse actual = controller.register(request);

        // Assert
        // same response and a CREATED response status annotation
        ResponseStatus status = AuthController.class.getMethod("register", RegisterRequest.class).getAnnotation(ResponseStatus.class);
        assertSame(expected, actual);
        verify(service).register(request);
        assertNotNull(status);
        assertEquals(HttpStatus.CREATED, status.value());
    }

    // MEDIUM
    @Test
    @DisplayName("login delegates to the service unchanged")
    void login_delegates_to_the_service_unchanged() throws Exception {
        // Arrange
        // service returns a canned response
        AuthService service = mock(AuthService.class);
        LoginRequest request = new LoginRequest();
        AuthResponse expected = AuthResponse.builder().accessToken("a").build();
        when(service.login(request)).thenReturn(expected);
        AuthController controller = new AuthController(service);

        // Act
        // call the endpoint method
        AuthResponse actual = controller.login(request);

        // Assert
        // pass-through only
        assertSame(expected, actual);
        verify(service).login(request);
        verifyNoMoreInteractions(service);
    }

    // MEDIUM
    @Test
    @DisplayName("refresh delegates to the service unchanged")
    void refresh_delegates_to_the_service_unchanged() throws Exception {
        // Arrange
        // service returns a canned response
        AuthService service = mock(AuthService.class);
        RefreshRequest request = new RefreshRequest();
        AuthResponse expected = AuthResponse.builder().accessToken("a").build();
        when(service.refresh(request)).thenReturn(expected);
        AuthController controller = new AuthController(service);

        // Act
        // call the endpoint method
        AuthResponse actual = controller.refresh(request);

        // Assert
        // pass-through only
        assertSame(expected, actual);
        verify(service).refresh(request);
        verifyNoMoreInteractions(service);
    }
}
