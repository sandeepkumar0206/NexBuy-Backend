package com.nexbuy.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import com.nexbuy.repository.*;
import com.nexbuy.testsupport.TestFixtures;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Unit tests for CustomUserDetailsService. */
class CustomUserDetailsServiceTest {

    @Test
    @DisplayName("loadUserByUsername maps a stored admin to credentials and a ROLE_ADMIN authority")
    void loaduserbyusername_maps_a_stored_admin_to_credentials_and_a_role_admin_authority() throws Exception {
        // Arrange
        // admin user in the repository
        UserRepository users = mock(UserRepository.class);
        when(users.findByEmail("admin@nexbuy.com")).thenReturn(Optional.of(TestFixtures.user(1L, "Admin", "admin@nexbuy.com", Role.ADMIN)));
        CustomUserDetailsService service = new CustomUserDetailsService(users);

        // Act
        // load by email
        UserDetails details = service.loadUserByUsername("admin@nexbuy.com");

        // Assert
        // username, hashed password, single ROLE_ADMIN authority
        assertEquals("admin@nexbuy.com", details.getUsername());
        assertEquals("hashed", details.getPassword());
        assertEquals(1, details.getAuthorities().size());
        assertEquals("ROLE_ADMIN", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException for an unknown email")
    void loaduserbyusername_throws_usernamenotfoundexception_for_an_unknown_email() throws Exception {
        // Arrange
        // empty repository
        UserRepository users = mock(UserRepository.class);
        when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        CustomUserDetailsService service = new CustomUserDetailsService(users);

        // Act
        // load a missing user
        UsernameNotFoundException error = assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost@example.com"));

        // Assert
        // message names the email and the repository was asked once
        assertEquals("User not found: ghost@example.com", error.getMessage());
        verify(users, times(1)).findByEmail("ghost@example.com");
        verifyNoMoreInteractions(users);
    }
}
