package com.cineBook.cinebook_platform.auth.dto;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    String userId;
    String name;
    String email;
    String role;
}