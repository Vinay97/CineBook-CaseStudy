package com.cineBook.cinebook_platform.controller;

import com.cineBook.cinebook_platform.auth.AuthService;
import com.cineBook.cinebook_platform.auth.dto.AuthResponse;
import com.cineBook.cinebook_platform.auth.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequiredArgsConstructor
public class LocalTokenController {

    private final AuthService authService;

    // Convenience endpoint — logs in a seeded user by role
    // GET /local/token?role=CUSTOMER
    // GET /local/token?role=THEATRE_PARTNER
    // GET /local/token?role=PLATFORM_ADMIN
    @GetMapping("/local/token")
    public ResponseEntity<AuthResponse> getTokenForRole(
            @RequestParam(defaultValue = "CUSTOMER") String role) {

        String email = switch (role) {
            case "PLATFORM_ADMIN"   -> "admin@cinebook.com";
            case "THEATRE_PARTNER"  -> "partner@pvr.com";
            default                 -> "customer@gmail.com";
        };

        String password = switch (role) {
            case "PLATFORM_ADMIN"   -> "Admin@123";
            case "THEATRE_PARTNER"  -> "Partner@123";
            default                 -> "Customer@123";
        };

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        return ResponseEntity.ok(authService.login(request));
    }
}
