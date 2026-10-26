package com.nexbuy.patterns.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.entity.*;
import org.junit.jupiter.api.DisplayName;

/** Day 2 | AC: builder used by AuthService and AdminUserSeeder | BR: default role USER */
class UserBuilderTest {

    // MEDIUM
    @Test
    @DisplayName("build defaults the role to USER")
    void build_defaults_the_role_to_user() throws Exception {
        // Arrange
        // builder without a role
        UserBuilder builder = new UserBuilder().name("Sam").email("sam@example.com").password("hashed");

        // Act
        // build
        User user = builder.build();

        // Assert
        // default role and mapped fields
        assertEquals(Role.USER, user.getRole());
        assertEquals("Sam", user.getName());
        assertEquals("sam@example.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
    }

    // MEDIUM
    @Test
    @DisplayName("build keeps an explicitly assigned role")
    void build_keeps_an_explicitly_assigned_role() throws Exception {
        // Arrange
        // builder with ADMIN
        UserBuilder builder = new UserBuilder().name("Root").email("root@example.com").password("hashed").role(Role.ADMIN);

        // Act
        // build
        User user = builder.build();

        // Assert
        // role respected
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("root@example.com", user.getEmail());
        assertNull(user.getId());
    }
}
