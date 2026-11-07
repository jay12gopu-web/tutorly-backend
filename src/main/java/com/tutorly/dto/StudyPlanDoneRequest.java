package com.tutorly.dto;

import jakarta.validation.constraints.NotNull;

public record StudyPlanDoneRequest(@NotNull Boolean done) {
}
