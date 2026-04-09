package com.cineBook.cinebook_platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateSeatsRequest {

    @NotBlank
    private String fromRow;
    @NotBlank private String toRow;
    @NotNull
    @Min(1) private Integer seatsPerRow;
}
