package com.nexbuy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.security.*;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Day 2 | AC: SecurityConfig (CORS, BCrypt password encoder) | BR: only configured origins, credentials allowed */
class SecurityConfigTest {

    // HIGH
    @Test
    @DisplayName("passwordEncoder produces verifiable BCrypt hashes")
    void passwordencoder_produces_verifiable_bcrypt_hashes() throws Exception {
        // Arrange
        // config with mocked collaborators
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class), mock(UserDetailsService.class));

        // Act
        // get the encoder
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        // BCrypt behavior
        String hash = encoder.encode("secret1");
        assertTrue(encoder instanceof BCryptPasswordEncoder);
        assertTrue(hash.startsWith("$2"));
        assertNotEquals("secret1", hash);
        assertTrue(encoder.matches("secret1", hash));
        assertFalse(encoder.matches("wrong", hash));
    }

    // HIGH
    @Test
    @DisplayName("corsConfigurationSource trims and filters the configured origins")
    void corsconfigurationsource_trims_and_filters_the_configured_origins() throws Exception {
        // Arrange
        // origins with spaces and an empty entry
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class), mock(UserDetailsService.class));
        ReflectionTestUtils.setField(config, "allowedOrigins", " http://localhost:4200 , ,https://nexbuy.app ");

        // Act
        // build the CORS source
        CorsConfigurationSource source = config.corsConfigurationSource();

        // Assert
        // clean origin list, methods, credentials, preflight cache
        CorsConfiguration cors = ((UrlBasedCorsConfigurationSource) source).getCorsConfigurations().get("/**");
        assertEquals(List.of("http://localhost:4200", "https://nexbuy.app"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")));
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
        assertEquals(3600L, cors.getMaxAge());
        assertEquals(List.of("*"), cors.getAllowedHeaders());
    }

    // HIGH
    @Test
    @DisplayName("authenticationProvider is a DAO provider for username/password tokens")
    void authenticationprovider_is_a_dao_provider_for_username_password_tokens() throws Exception {
        // Arrange
        // config with a mocked user details service
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class), mock(UserDetailsService.class));

        // Act
        // build the provider
        AuthenticationProvider provider = config.authenticationProvider();

        // Assert
        // type and supported token class
        assertTrue(provider instanceof DaoAuthenticationProvider);
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(provider.supports(Object.class));
    }
}
