package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterTheatreRequest {

    @NotBlank
    private String name;
    @NotBlank private String address;
    @NotBlank private String city;
    private String state;
    @NotBlank private String country;
    private String pincode;
    private String contactEmail;
    private String contactPhone;
}
