package com.cloudnotes.auth;

import com.cloudnotes.common.ApiResponse;
import com.cloudnotes.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> register(@RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> refresh(@RequestBody AuthDto.RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refreshToken(request.getRefreshToken())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserDto>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentUserProfile(user)));
    }
}
