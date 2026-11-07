package com.tutorly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRequest(

        @NotBlank
        @Size(max = 80)
        String name,

        @NotBlank
        @Email
        @Size(max = 200)
        String email,

        @NotBlank
        @Size(max = 30)
        String grade,

        @NotBlank
        @Size(max = 80)
        String city
) {
}