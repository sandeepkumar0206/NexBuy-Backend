package com.nexbuy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import com.nexbuy.repository.*;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests for AdminUserSeeder. */
class AdminUserSeederTest {

    @Test
    @DisplayName("run seeds a hashed ADMIN user when none exists")
    void run_seeds_a_hashed_admin_user_when_none_exists() throws Exception {
        // Arrange
        // configured admin credentials with padded mixed-case email
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AdminUserSeeder seeder = new AdminUserSeeder(users, encoder);
        ReflectionTestUtils.setField(seeder, "adminEmail", " Admin@NexBuy.com ");
        ReflectionTestUtils.setField(seeder, "adminPassword", "Admin@123");
        ReflectionTestUtils.setField(seeder, "adminName", "NexBuy Admin");
        when(users.existsByEmail("admin@nexbuy.com")).thenReturn(false);
        when(encoder.encode("Admin@123")).thenReturn("encoded-admin");

        // Act
        // run the seeder
        seeder.run();

        // Assert
        // saved admin is normalized and hashed
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("admin@nexbuy.com", saved.getValue().getEmail());
        assertEquals(Role.ADMIN, saved.getValue().getRole());
        assertEquals("encoded-admin", saved.getValue().getPassword());
        assertEquals("NexBuy Admin", saved.getValue().getName());
    }

    @Test
    @DisplayName("run does nothing when the admin already exists")
    void run_does_nothing_when_the_admin_already_exists() throws Exception {
        // Arrange
        // admin present
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        AdminUserSeeder seeder = new AdminUserSeeder(users, encoder);
        ReflectionTestUtils.setField(seeder, "adminEmail", "admin@nexbuy.com");
        ReflectionTestUtils.setField(seeder, "adminPassword", "Admin@123");
        ReflectionTestUtils.setField(seeder, "adminName", "NexBuy Admin");
        when(users.existsByEmail("admin@nexbuy.com")).thenReturn(true);

        // Act
        // run the seeder
        seeder.run();

        // Assert
        // idempotent: no hash, no save
        verify(users).existsByEmail("admin@nexbuy.com");
        verify(users, never()).save(any(User.class));
        verify(encoder, never()).encode(anyString());
        verifyNoMoreInteractions(users);
    }
}
