package com.nexbuy.service;

import com.nexbuy.dto.auth.AuthResponse;
import com.nexbuy.dto.auth.LoginRequest;
import com.nexbuy.dto.auth.RefreshRequest;
import com.nexbuy.dto.auth.RegisterRequest;
import com.nexbuy.entity.Role;
import com.nexbuy.entity.User;
import com.nexbuy.exception.ApiException;
import com.nexbuy.patterns.builder.UserBuilder;
import com.nexbuy.repository.UserRepository;
import com.nexbuy.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ApiException("Email is already registered");
        }

        User user = new UserBuilder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ApiException("Invalid email or password"));

        return toAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        try {
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }
            String email = jwtService.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
            if (!jwtService.isTokenValid(refreshToken, email)) {
                throw new ApiException("Refresh token expired", HttpStatus.UNAUTHORIZED);
            }
            return toAuthResponse(user);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    private AuthResponse toAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
