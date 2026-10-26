package com.nexbuy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.dto.auth.*;
import com.nexbuy.entity.*;
import com.nexbuy.exception.ApiException;
import com.nexbuy.repository.*;
import com.nexbuy.security.*;
import com.nexbuy.testsupport.TestFixtures;
import io.jsonwebtoken.JwtException;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Day 2 | AC: register/login/refresh API return tokens | BR: unique email, BCrypt hash, default role USER */
class AuthServiceTest {

    // CRITICAL
    @Test
    @DisplayName("register creates a USER account with trimmed lowercase email and hashed password")
    void register_creates_a_user_account_with_trimmed_lowercase_email_and_hashed_password() throws Exception {
        // Arrange
        // valid request with padded mixed-case input, free email, stubbed encoder and token service
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        RegisterRequest request = new RegisterRequest();
        request.setName("  Sam  ");
        request.setEmail("  Sam@Example.COM ");
        request.setPassword("secret1");
        when(users.existsByEmail("sam@example.com")).thenReturn(false);
        when(encoder.encode("secret1")).thenReturn("hashed-secret");
        when(jwt.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwt.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        AuthService service = new AuthService(users, encoder, jwt, mock(AuthenticationManager.class));

        // Act
        // register once
        AuthResponse response = service.register(request);

        // Assert
        // saved user is normalized and hashed; response carries both tokens
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("sam@example.com", saved.getValue().getEmail());
        assertEquals("Sam", saved.getValue().getName());
        assertEquals("hashed-secret", saved.getValue().getPassword());
        assertEquals(Role.USER, saved.getValue().getRole());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }

    // CRITICAL
    @Test
    @DisplayName("register rejects an email that is already registered")
    void register_rejects_an_email_that_is_already_registered() throws Exception {
        // Arrange
        // email already exists
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        JwtService jwt = mock(JwtService.class);
        RegisterRequest request = new RegisterRequest();
        request.setName("Sam");
        request.setEmail("Sam@Example.com");
        request.setPassword("secret1");
        when(users.existsByEmail("sam@example.com")).thenReturn(true);
        AuthService service = new AuthService(users, encoder, jwt, mock(AuthenticationManager.class));

        // Act
        // register a duplicate
        ApiException error = assertThrows(ApiException.class, () -> service.register(request));

        // Assert
        // 400 with message and nothing persisted
        assertEquals("Email is already registered", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(users, never()).save(any(User.class));
        verify(encoder, never()).encode(anyString());
        verify(jwt, never()).generateAccessToken(any(User.class));
    }

    // CRITICAL
    @Test
    @DisplayName("login authenticates normalized credentials and returns the user's tokens")
    void login_authenticates_normalized_credentials_and_returns_the_user_s_tokens() throws Exception {
        // Arrange
        // authentication succeeds and the stored user exists
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        User stored = TestFixtures.user(7L, "Sam", "sam@example.com", Role.USER);
        LoginRequest request = new LoginRequest();
        request.setEmail(" SAM@example.com ");
        request.setPassword("secret1");
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(stored));
        when(jwt.generateAccessToken(stored)).thenReturn("access-token");
        when(jwt.generateRefreshToken(stored)).thenReturn("refresh-token");
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, manager);

        // Act
        // login once
        AuthResponse response = service.login(request);

        // Assert
        // manager saw the normalized email and raw password; response mirrors the stored user
        ArgumentCaptor<UsernamePasswordAuthenticationToken> token = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(manager).authenticate(token.capture());
        assertEquals("sam@example.com", token.getValue().getPrincipal());
        assertEquals("secret1", token.getValue().getCredentials());
        assertEquals(7L, response.getUserId());
        assertEquals("Sam", response.getName());
        assertEquals(Role.USER, response.getRole());
        assertEquals("access-token", response.getAccessToken());
    }

    // CRITICAL
    @Test
    @DisplayName("login lets bad credentials propagate without loading the user")
    void login_lets_bad_credentials_propagate_without_loading_the_user() throws Exception {
        // Arrange
        // authentication manager rejects the credentials
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        LoginRequest request = new LoginRequest();
        request.setEmail("sam@example.com");
        request.setPassword("wrong");
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, manager);

        // Act
        // login with wrong password
        BadCredentialsException error = assertThrows(BadCredentialsException.class, () -> service.login(request));

        // Assert
        // error surfaces; no user lookup or token issued
        assertEquals("Bad credentials", error.getMessage());
        verify(manager, times(1)).authenticate(any());
        verify(users, never()).findByEmail(anyString());
        verify(jwt, never()).generateAccessToken(any(User.class));
    }

    // CRITICAL
    @Test
    @DisplayName("login fails with the generic message when the user vanished after authentication")
    void login_fails_with_the_generic_message_when_the_user_vanished_after_authentication() throws Exception {
        // Arrange
        // authentication passes but the repository has no such user
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@example.com");
        request.setPassword("secret1");
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // login
        ApiException error = assertThrows(ApiException.class, () -> service.login(request));

        // Assert
        // same wording as bad credentials, no tokens
        assertEquals("Invalid email or password", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(users).findByEmail("ghost@example.com");
        verify(jwt, never()).generateRefreshToken(any(User.class));
    }

    // CRITICAL
    @Test
    @DisplayName("refresh issues new tokens for a valid refresh token")
    void refresh_issues_new_tokens_for_a_valid_refresh_token() throws Exception {
        // Arrange
        // refresh token is typed refresh, unexpired, and its user exists
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        User stored = TestFixtures.user(7L, "Sam", "sam@example.com", Role.USER);
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-1");
        when(jwt.isRefreshToken("refresh-1")).thenReturn(true);
        when(jwt.extractEmail("refresh-1")).thenReturn("sam@example.com");
        when(jwt.isTokenValid("refresh-1", "sam@example.com")).thenReturn(true);
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(stored));
        when(jwt.generateAccessToken(stored)).thenReturn("new-access");
        when(jwt.generateRefreshToken(stored)).thenReturn("new-refresh");
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // refresh once
        AuthResponse response = service.refresh(request);

        // Assert
        // rotated tokens for the same user
        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        assertEquals("sam@example.com", response.getEmail());
        assertEquals(7L, response.getUserId());
        verify(jwt).isTokenValid("refresh-1", "sam@example.com");
    }

    // CRITICAL
    @Test
    @DisplayName("refresh rejects an access token with 401")
    void refresh_rejects_an_access_token_with_401() throws Exception {
        // Arrange
        // token is not typed refresh
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("access-1");
        when(jwt.isRefreshToken("access-1")).thenReturn(false);
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // refresh with the wrong token type
        ApiException error = assertThrows(ApiException.class, () -> service.refresh(request));

        // Assert
        // 401 and no further lookups
        assertEquals("Invalid refresh token", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
        verify(jwt, never()).extractEmail(anyString());
        verify(users, never()).findByEmail(anyString());
    }

    // CRITICAL
    @Test
    @DisplayName("refresh returns 404 when the token's user no longer exists")
    void refresh_returns_404_when_the_token_s_user_no_longer_exists() throws Exception {
        // Arrange
        // valid refresh token for a deleted user
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-1");
        when(jwt.isRefreshToken("refresh-1")).thenReturn(true);
        when(jwt.extractEmail("refresh-1")).thenReturn("gone@example.com");
        when(users.findByEmail("gone@example.com")).thenReturn(Optional.empty());
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // refresh
        ApiException error = assertThrows(ApiException.class, () -> service.refresh(request));

        // Assert
        // 404 and expiry never checked
        assertEquals("User not found", error.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(jwt, never()).isTokenValid(anyString(), anyString());
    }

    // CRITICAL
    @Test
    @DisplayName("refresh returns 401 when the refresh token is no longer valid")
    void refresh_returns_401_when_the_refresh_token_is_no_longer_valid() throws Exception {
        // Arrange
        // user exists but the token fails validation
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        User stored = TestFixtures.user(7L, "Sam", "sam@example.com", Role.USER);
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-1");
        when(jwt.isRefreshToken("refresh-1")).thenReturn(true);
        when(jwt.extractEmail("refresh-1")).thenReturn("sam@example.com");
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(stored));
        when(jwt.isTokenValid("refresh-1", "sam@example.com")).thenReturn(false);
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // refresh with an expired token
        ApiException error = assertThrows(ApiException.class, () -> service.refresh(request));

        // Assert
        // 401 and no new tokens
        assertEquals("Refresh token expired", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
        verify(jwt, never()).generateAccessToken(any(User.class));
    }

    // CRITICAL
    @Test
    @DisplayName("refresh converts a malformed token failure into a 401")
    void refresh_converts_a_malformed_token_failure_into_a_401() throws Exception {
        // Arrange
        // token parser blows up with a JwtException
        UserRepository users = mock(UserRepository.class);
        JwtService jwt = mock(JwtService.class);
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("garbage");
        when(jwt.isRefreshToken("garbage")).thenThrow(new JwtException("malformed"));
        AuthService service = new AuthService(users, mock(PasswordEncoder.class), jwt, mock(AuthenticationManager.class));

        // Act
        // refresh with garbage
        ApiException error = assertThrows(ApiException.class, () -> service.refresh(request));

        // Assert
        // generic 401 that hides parser details
        assertEquals("Invalid refresh token", error.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatus());
        verify(users, never()).findByEmail(anyString());
    }
}
