package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddScreenRequest {

    @NotBlank
    private String name;
    @NotNull
    @Min(1) private Integer totalSeats;
    @NotBlank private String screenType;
}
