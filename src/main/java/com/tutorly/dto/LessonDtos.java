package com.tutorly.dto;

import com.tutorly.model.Lesson;
import com.tutorly.model.LessonProgress;
import com.tutorly.model.LessonStep;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public final class LessonDtos {

    private LessonDtos() {
    }

    public record Summary(Long id, String title, String subject,
                          String topic, int estimatedMinutes) {
        public static Summary from(Lesson lesson) {
            return new Summary(lesson.getId(), lesson.getTitle(), lesson.getSubject(),
                    lesson.getTopic(), lesson.getEstimatedMinutes());
        }
    }

    public record Step(Long id, int stepNumber, String heading,
                       String body, String diagramMermaid) {
        public static Step from(LessonStep step) {
            return new Step(step.getId(), step.getStepNumber(), step.getHeading(),
                    step.getBody(), step.getDiagramMermaid());
        }
    }

    public record Detail(Long id, String title, String subject, String topic,
                         int estimatedMinutes, List<Step> steps) {
        public static Detail from(Lesson lesson) {
            return new Detail(lesson.getId(), lesson.getTitle(), lesson.getSubject(),
                    lesson.getTopic(), lesson.getEstimatedMinutes(),
                    lesson.getSteps().stream().map(Step::from).toList());
        }
    }

    public record UpdateProgress(@NotNull @Min(1) Integer completedSteps) {
    }

    public record Progress(Long lessonId, String title, int completedSteps,
                           int totalSteps, boolean completed, Integer nextStep,
                           Instant updatedAt) {
        public static Progress from(Lesson lesson, LessonProgress progress) {
            int total = lesson.getSteps().size();
            int done = progress == null ? 0 : Math.min(progress.getCompletedSteps(), total);
            boolean completed = total > 0 && done == total;
            Integer next = done < total ? done + 1 : null;
            return new Progress(lesson.getId(), lesson.getTitle(), done, total,
                    completed, next, progress == null ? null : progress.getUpdatedAt());
        }
    }
}
