package com.tutorly.service;

import com.tutorly.auth.JwtAuthService;
import com.tutorly.dto.*;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private static final int PREVIEW_LIMIT = 5;
    private final JwtAuthService auth;
    private final StudyPlanService plans;
    private final LessonService lessons;
    private final QuizService quizzes;
    private final Clock clock;

    @Autowired
    public DashboardService(JwtAuthService auth, StudyPlanService plans,
                            LessonService lessons, QuizService quizzes) {
        this(auth, plans, lessons, quizzes, Clock.systemUTC());
    }

    // Explicit clock lets tests exercise the exact start-time boundary.
    public DashboardService(JwtAuthService auth, StudyPlanService plans,
                            LessonService lessons, QuizService quizzes, Clock clock) {
        this.auth = auth;
        this.plans = plans;
        this.lessons = lessons;
        this.quizzes = quizzes;
        this.clock = clock;
    }

    public DashboardResponse get(Long studentId) {
        var student = auth.me(studentId);
        Instant now = clock.instant();
        // Reuse the existing services: all reads are scoped to this student's ID.
        var allPlans = plans.getAll(studentId);
        var allProgress = lessons.history(studentId);
        var allAttempts = quizzes.history(studentId);

        var upcoming = allPlans.stream()
                .filter(plan -> !plan.done() && !plan.startsAt().isBefore(now))
                .sorted(Comparator.comparing(StudyPlanResponse::startsAt)
                        .thenComparing(StudyPlanResponse::id))
                .toList();
        long completedPlans = allPlans.stream().filter(StudyPlanResponse::done).count();
        long completedLessons = allProgress.stream().filter(LessonDtos.Progress::completed).count();
        Integer best = allAttempts.isEmpty() ? null
                : allAttempts.stream().mapToInt(QuizDtos.Attempt::score).max().orElseThrow();
        Double average = allAttempts.isEmpty() ? null
                : Math.round(allAttempts.stream().mapToInt(QuizDtos.Attempt::score)
                        .average().orElseThrow() * 10.0) / 10.0;

        var stats = new DashboardResponse.Stats(
                allPlans.size(), completedPlans, allPlans.size() - completedPlans,
                upcoming.size(), allProgress.size(), completedLessons,
                allProgress.size() - completedLessons, allAttempts.size(), best, average);
        var recentProgress = allProgress.stream()
                .sorted(Comparator.comparing(LessonDtos.Progress::updatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(LessonDtos.Progress::lessonId, Comparator.reverseOrder()))
                .limit(PREVIEW_LIMIT).toList();
        var recentAttempts = allAttempts.stream()
                .sorted(Comparator.comparing(QuizDtos.Attempt::createdAt, Comparator.reverseOrder())
                        .thenComparing(QuizDtos.Attempt::id, Comparator.reverseOrder()))
                .limit(PREVIEW_LIMIT).toList();

        return new DashboardResponse(student, now, stats,
                upcoming.stream().limit(PREVIEW_LIMIT).toList(), recentProgress, recentAttempts);
    }
}
