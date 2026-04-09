package com.cineBook.cinebook_platform.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePartnerRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String password;
    // No theatreId here — partner registers their theatre themselves
    // after their account is created via POST /theatre-admin/theatre
}