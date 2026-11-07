package com.tutorly.dto;

import jakarta.validation.constraints.*;
import com.tutorly.model.Student;

public final class JwtAuthDtos {
    private JwtAuthDtos() {}
    public record Register(
            @NotBlank @Size(max=80) String name,
            @NotBlank @Email @Size(max=200) String email,
            @NotBlank @Size(max=30) String grade,
            @NotBlank @Size(max=80) String city,
            @NotBlank @Size(min=10, max=72) String password) {}
    public record Login(
            @NotBlank @Email @Size(max=200) String email,
            @NotBlank @Size(max=72) String password) {}
    public record Profile(Long id, String name, String email, String grade, String city) {
        public static Profile from(Student s) {
            return new Profile(s.getId(), s.getName(), s.getEmail(), s.getGrade(), s.getCity());
        }
    }
    public record Token(String accessToken, String tokenType, long expiresIn, Long studentId) {}
}
