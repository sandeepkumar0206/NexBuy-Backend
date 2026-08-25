package com.nexbuy.patterns.builder;

import com.nexbuy.entity.Role;
import com.nexbuy.entity.User;

/** Explicit Builder for User (alongside Lombok builders on entities). */
public class UserBuilder {
    private String name;
    private String email;
    private String password;
    private Role role = Role.USER;

    public UserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder role(Role role) {
        this.role = role;
        return this;
    }

    public User build() {
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }
}
