package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.auth.AuthService;
import com.cineBook.cinebook_platform.auth.dto.AuthResponse;
import com.cineBook.cinebook_platform.auth.dto.LoginRequest;
import com.cineBook.cinebook_platform.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/register
    // Self-registration — always creates a CUSTOMER
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // POST /api/v1/auth/login
    // Works for all roles — token carries the role claim
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
