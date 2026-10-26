package com.nexbuy.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import io.jsonwebtoken.JwtException;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Day 2 | AC: JwtAuthFilter (OncePerRequestFilter) | BR: bad tokens never block public endpoints */
class JwtAuthFilterTest {

    // CRITICAL
    @Test
    @DisplayName("a request without an Authorization header passes through anonymous")
    void a_request_without_an_authorization_header_passes_through_anonymous() throws Exception {
        // Arrange
        // no header, mocked collaborators
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // chain continues, nobody is authenticated, no token work done
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
        verifyNoInteractions(jwt);
        verifyNoInteractions(users);
    }

    // CRITICAL
    @Test
    @DisplayName("a non-Bearer Authorization header is ignored")
    void a_non_bearer_authorization_header_is_ignored() throws Exception {
        // Arrange
        // Basic auth header
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // untouched by JWT logic
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(jwt);
        verifyNoInteractions(users);
    }

    // CRITICAL
    @Test
    @DisplayName("a valid access token authenticates the request with the user's roles")
    void a_valid_access_token_authenticates_the_request_with_the_user_s_roles() throws Exception {
        // Arrange
        // good access token for sam with ROLE_USER
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        UserDetails details = new org.springframework.security.core.userdetails.User(
                "sam@example.com", "hashed", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(jwt.extractTokenType("good-token")).thenReturn("access");
        when(jwt.extractEmail("good-token")).thenReturn("sam@example.com");
        when(users.loadUserByUsername("sam@example.com")).thenReturn(details);
        when(jwt.isTokenValid("good-token", "sam@example.com")).thenReturn(true);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // authenticated principal with the role, chain continues
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("sam@example.com", ((UserDetails) authentication.getPrincipal()).getUsername());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
        assertNotNull(chain.getRequest());
        SecurityContextHolder.clearContext();
    }

    // CRITICAL
    @Test
    @DisplayName("a refresh token is not accepted as an access credential")
    void a_refresh_token_is_not_accepted_as_an_access_credential() throws Exception {
        // Arrange
        // token typed refresh
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        when(jwt.extractTokenType("refresh-token")).thenReturn("refresh");
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer refresh-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // anonymous, user never loaded
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwt, never()).extractEmail(anyString());
        verify(users, never()).loadUserByUsername(anyString());
        assertNotNull(chain.getRequest());
    }

    // CRITICAL
    @Test
    @DisplayName("an unparseable token is swallowed and the request continues anonymous")
    void an_unparseable_token_is_swallowed_and_the_request_continues_anonymous() throws Exception {
        // Arrange
        // token parsing throws
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        when(jwt.extractTokenType("bad-token")).thenThrow(new JwtException("bad token"));
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // no 401 from the filter itself, context empty
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
        verify(users, never()).loadUserByUsername(anyString());
    }

    // CRITICAL
    @Test
    @DisplayName("a token that fails validation does not authenticate")
    void a_token_that_fails_validation_does_not_authenticate() throws Exception {
        // Arrange
        // valid signature but isTokenValid is false
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        UserDetails details = new org.springframework.security.core.userdetails.User(
                "sam@example.com", "hashed", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(jwt.extractTokenType("stale-token")).thenReturn("access");
        when(jwt.extractEmail("stale-token")).thenReturn("sam@example.com");
        when(users.loadUserByUsername("sam@example.com")).thenReturn(details);
        when(jwt.isTokenValid("stale-token", "sam@example.com")).thenReturn(false);
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // user loaded once but no authentication set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(users, times(1)).loadUserByUsername("sam@example.com");
        assertNotNull(chain.getRequest());
    }

    // CRITICAL
    @Test
    @DisplayName("a token for an unknown user is swallowed and the request continues anonymous")
    void a_token_for_an_unknown_user_is_swallowed_and_the_request_continues_anonymous() throws Exception {
        // Arrange
        // user lookup throws UsernameNotFoundException
        SecurityContextHolder.clearContext();
        JwtService jwt = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        when(jwt.extractTokenType("orphan-token")).thenReturn("access");
        when(jwt.extractEmail("orphan-token")).thenReturn("gone@example.com");
        when(users.loadUserByUsername("gone@example.com")).thenThrow(new UsernameNotFoundException("gone"));
        JwtAuthFilter filter = new JwtAuthFilter(jwt, users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer orphan-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Act
        // run the filter
        filter.doFilter(request, response, chain);

        // Assert
        // anonymous and the chain still ran
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
        verify(jwt, never()).isTokenValid(anyString(), anyString());
    }
}
