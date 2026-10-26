package com.nexbuy.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import com.nexbuy.testsupport.TestFixtures;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.*;
import org.junit.jupiter.api.DisplayName;

/** Day 2 | AC: JWT utility generates and validates tokens | BR: access vs refresh type, signed, expiring */
class JwtServiceTest {

    // CRITICAL
    @Test
    @DisplayName("generateAccessToken signs a three-part token typed access for the user's email")
    void generateaccesstoken_signs_a_three_part_token_typed_access_for_the_user_s_email() throws Exception {
        // Arrange
        // service with a 60s access lifetime and a normal user
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", 60_000L, 120_000L);
        User user = TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER);

        // Act
        // issue an access token
        String token = jwt.generateAccessToken(user);

        // Assert
        // subject, type, and structure
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertEquals("access", jwt.extractTokenType(token));
        assertFalse(jwt.isRefreshToken(token));
        assertEquals(3, token.split("\\.").length);
    }

    // CRITICAL
    @Test
    @DisplayName("generateRefreshToken is typed refresh and recognized by isRefreshToken")
    void generaterefreshtoken_is_typed_refresh_and_recognized_by_isrefreshtoken() throws Exception {
        // Arrange
        // same service and user
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", 60_000L, 120_000L);
        User user = TestFixtures.user(3L, "Sam", "sam@example.com", Role.ADMIN);

        // Act
        // issue a refresh token
        String token = jwt.generateRefreshToken(user);

        // Assert
        // type and subject
        assertEquals("refresh", jwt.extractTokenType(token));
        assertTrue(jwt.isRefreshToken(token));
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertTrue(jwt.isTokenValid(token, "sam@example.com"));
    }

    // CRITICAL
    @Test
    @DisplayName("isTokenValid accepts an unexpired token for the matching email")
    void istokenvalid_accepts_an_unexpired_token_for_the_matching_email() throws Exception {
        // Arrange
        // fresh token
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", 60_000L, 120_000L);
        String token = jwt.generateAccessToken(TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER));

        // Act
        // validate for the owner
        boolean valid = jwt.isTokenValid(token, "sam@example.com");

        // Assert
        // valid and still readable
        assertTrue(valid);
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertEquals("access", jwt.extractTokenType(token));
    }

    // CRITICAL
    @Test
    @DisplayName("isTokenValid rejects a different email")
    void istokenvalid_rejects_a_different_email() throws Exception {
        // Arrange
        // token owned by sam
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", 60_000L, 120_000L);
        String token = jwt.generateAccessToken(TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER));

        // Act
        // validate for someone else
        boolean valid = jwt.isTokenValid(token, "other@example.com");

        // Assert
        // false while the token itself stays well-formed
        assertFalse(valid);
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertNotEquals("other@example.com", jwt.extractEmail(token));
    }

    // CRITICAL
    @Test
    @DisplayName("parsing an expired token throws ExpiredJwtException")
    void parsing_an_expired_token_throws_expiredjwtexception() throws Exception {
        // Arrange
        // token that expired one second ago
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", -1_000L, 120_000L);
        String token = jwt.generateAccessToken(TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER));

        // Act
        // validate the expired token
        ExpiredJwtException error = assertThrows(ExpiredJwtException.class, () -> jwt.isTokenValid(token, "sam@example.com"));

        // Assert
        // every parsing accessor refuses it
        assertNotNull(error.getClaims());
        assertThrows(ExpiredJwtException.class, () -> jwt.extractEmail(token));
        assertThrows(ExpiredJwtException.class, () -> jwt.extractTokenType(token));
    }

    // CRITICAL
    @Test
    @DisplayName("a token signed with another secret is rejected")
    void a_token_signed_with_another_secret_is_rejected() throws Exception {
        // Arrange
        // token produced by a service with a different key
        JwtService jwt = new JwtService("utc-test-secret-utc-test-secret-0123456789", 60_000L, 120_000L);
        JwtService foreign = new JwtService("another-secret-another-secret-9876543210", 60_000L, 120_000L);
        String token = foreign.generateAccessToken(TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER));

        // Act
        // parse with the wrong key
        SignatureException error = assertThrows(SignatureException.class, () -> jwt.extractEmail(token));

        // Assert
        // signature failure; the issuing service still accepts its own token
        assertNotNull(error.getMessage());
        assertEquals("sam@example.com", foreign.extractEmail(token));
        assertThrows(SignatureException.class, () -> jwt.isTokenValid(token, "sam@example.com"));
    }

    // CRITICAL
    @Test
    @DisplayName("a short secret is padded to a usable signing key")
    void a_short_secret_is_padded_to_a_usable_signing_key() throws Exception {
        // Arrange
        // 5-character secret is below the 256-bit minimum
        User user = TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER);

        // Act
        // construct and issue a token
        JwtService jwt = new JwtService("short", 60_000L, 120_000L);
        String token = jwt.generateAccessToken(user);

        // Assert
        // token round-trips
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertEquals("access", jwt.extractTokenType(token));
        assertEquals(3, token.split("\\.").length);
    }

    // CRITICAL
    @Test
    @DisplayName("a base64 secret is decoded before use")
    void a_base64_secret_is_decoded_before_use() throws Exception {
        // Arrange
        // 32 bytes encoded as base64
        byte[] raw = new byte[32];
        Arrays.fill(raw, (byte) 7);
        String secret = Base64.getEncoder().encodeToString(raw);
        User user = TestFixtures.user(3L, "Sam", "sam@example.com", Role.USER);

        // Act
        // construct and issue a token
        JwtService jwt = new JwtService(secret, 60_000L, 120_000L);
        String token = jwt.generateRefreshToken(user);

        // Assert
        // token round-trips with the refresh type
        assertEquals("sam@example.com", jwt.extractEmail(token));
        assertTrue(jwt.isRefreshToken(token));
        assertEquals(3, token.split("\\.").length);
    }
}
