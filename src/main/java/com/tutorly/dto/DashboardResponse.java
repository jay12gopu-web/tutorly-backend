package com.tutorly.dto;

import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        JwtAuthDtos.Profile student,
        Instant generatedAt,
        Stats stats,
        List<StudyPlanResponse> upcomingStudyPlans,
        List<LessonDtos.Progress> recentLessonProgress,
        List<QuizDtos.Attempt> recentQuizAttempts
) {
    public record Stats(
            long studyPlansTotal,
            long studyPlansCompleted,
            long studyPlansPending,
            long studyPlansUpcoming,
            long lessonsStarted,
            long lessonsCompleted,
            long lessonsInProgress,
            long quizAttempts,
            Integer bestQuizScore,
            Double averageQuizScore
    ) {}
}
