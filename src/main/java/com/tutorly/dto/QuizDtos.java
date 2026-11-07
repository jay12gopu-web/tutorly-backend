package com.tutorly.dto;

import com.tutorly.model.Quiz;
import com.tutorly.model.QuizAttempt;
import com.tutorly.model.QuizQuestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public final class QuizDtos {

    private QuizDtos() {
    }

    public record Summary(Long id, String title, String subject, String topic) {
        public static Summary from(Quiz quiz) {
            return new Summary(quiz.getId(), quiz.getTitle(), quiz.getSubject(), quiz.getTopic());
        }
    }

    public record Question(Long id, String prompt, List<String> options) {
        public static Question from(QuizQuestion question) {
            return new Question(question.getId(), question.getPrompt(), question.getOptions());
        }
    }

    public record Detail(Long id, String title, String subject, String topic,
                         List<Question> questions) {
        public static Detail from(Quiz quiz) {
            return new Detail(quiz.getId(), quiz.getTitle(), quiz.getSubject(), quiz.getTopic(),
                    quiz.getQuestions().stream().map(Question::from).toList());
        }
    }

    public record Answer(
            @NotNull @Positive Long questionId,
            @NotNull @Min(0) @Max(3) Integer selectedOption
    ) {
    }

    public record Submission(
            @NotEmpty @Size(max = 100) List<@NotNull @Valid Answer> answers
    ) {
    }

    public record Attempt(Long id, Long quizId, String title, int correct,
                          int total, int score, Instant createdAt) {
        public static Attempt from(QuizAttempt attempt) {
            return new Attempt(attempt.getId(), attempt.getQuiz().getId(),
                    attempt.getQuizTitle(), attempt.getCorrect(), attempt.getTotal(),
                    attempt.getScore(), attempt.getCreatedAt());
        }
    }
}
