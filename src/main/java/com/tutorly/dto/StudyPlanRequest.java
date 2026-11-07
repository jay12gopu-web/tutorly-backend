package com.tutorly.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record StudyPlanRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 60) String subject,
        @NotNull @Future Instant startsAt,
        @NotNull @Min(5) @Max(240) Integer durationMinutes
) {
}
