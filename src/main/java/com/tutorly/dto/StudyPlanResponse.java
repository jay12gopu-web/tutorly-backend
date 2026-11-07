package com.tutorly.dto;

import com.tutorly.model.StudyPlan;
import java.time.Instant;

public record StudyPlanResponse(
        Long id,
        String title,
        String subject,
        Instant startsAt,
        int durationMinutes,
        boolean done
) {
    public static StudyPlanResponse from(StudyPlan plan) {
        return new StudyPlanResponse(
                plan.getId(), plan.getTitle(), plan.getSubject(),
                plan.getStartsAt(), plan.getDurationMinutes(), plan.isDone()
        );
    }
}
