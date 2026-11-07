package com.tutorly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(max = 80)
        String name,

        @NotBlank
        @Email
        @Size(max = 200)
        String email,

        @NotBlank
        @Size(min = 10, max = 72)
        String password,

        @NotBlank
        @Size(max = 30)
        String grade,

        @NotBlank
        @Size(max = 80)
        String city
) {
}