package com.cloudnotes.auth;

import com.cloudnotes.user.Role;
import com.cloudnotes.user.User;
import com.cloudnotes.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .branch(request.getBranch() != null ? request.getBranch().trim() : null)
                .semester(request.getSemester())
                .roles(Set.of(Role.ROLE_STUDENT))
                .enabled(true)
                .emailVerified(true)
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account is disabled. Please contact support.");
        }

        return buildAuthResponse(user);
    }

    public AuthDto.AuthResponse refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for token."));

        return buildAuthResponse(user);
    }

    public AuthDto.UserDto getCurrentUserProfile(User user) {
        return mapToUserDto(user);
    }

    private AuthDto.AuthResponse buildAuthResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthDto.UserDto mapToUserDto(User user) {
        return AuthDto.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .branch(user.getBranch())
                .semester(user.getSemester())
                .roles(user.getRoles())
                .build();
    }
}
