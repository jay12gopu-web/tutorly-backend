package com.tutorly.dto;

import com.tutorly.model.Teacher;
import com.tutorly.model.Teacher.TeachingMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class TeacherDtos {
    private TeacherDtos() {}
    public record ProfileInput(
            @NotBlank @Size(max=80) String name,
            @NotBlank @Size(max=80) String city,
            @NotBlank @Size(max=80) String area,
            @NotBlank @Size(max=1000) String bio,
            @NotBlank @Size(max=200) String qualification,
            @NotNull @Min(0) @Max(80) Integer experienceYears,
            @NotNull @DecimalMin("0.00") @DecimalMax("100000.00") @Digits(integer=6, fraction=2) BigDecimal hourlyRate,
            @NotNull TeachingMode teachingMode,
            @NotEmpty @Size(max=10) List<@NotBlank @Size(max=60) String> subjects) {}
    public record Register(
            @NotBlank @Email @Size(max=200) String email,
            @NotBlank @Size(min=10,max=72) String password,
            @NotNull @Valid ProfileInput profile) {}
    public record Login(
            @NotBlank @Email @Size(max=200) String email,
            @NotBlank @Size(max=72) String password) {}
    public record Update(@NotNull @Valid ProfileInput profile, @NotNull Boolean listed) {}
    public record Token(String accessToken, String tokenType, long expiresIn, Long teacherId) {}
    public record PublicProfile(Long id, String name, String city, String area, String bio,
                                String qualification, int experienceYears, BigDecimal hourlyRate,
                                String currency, TeachingMode teachingMode, List<String> subjects) {
        public static PublicProfile from(Teacher t) {
            return new PublicProfile(t.getId(),t.getName(),t.getCity(),t.getArea(),t.getBio(),
                    t.getQualification(),t.getExperienceYears(),t.getHourlyRate(),"INR",
                    t.getTeachingMode(),List.copyOf(t.getSubjects()));
        }
    }
    public record OwnProfile(String email, boolean listed, PublicProfile profile) {
        public static OwnProfile from(Teacher t) { return new OwnProfile(t.getEmail(),t.isListed(),PublicProfile.from(t)); }
    }
    public record SearchResult(List<PublicProfile> items, int page, int size, long totalElements,
                               int totalPages, boolean hasNext) {}
}
