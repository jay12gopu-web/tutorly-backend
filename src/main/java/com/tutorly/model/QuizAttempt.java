package com.tutorly.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_quiz_attempts_student_created", columnList = "student_id,created_at")
})
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_attempts_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_attempts_quiz"))
    private Quiz quiz;

    @Column(name = "quiz_title", nullable = false, length = 120)
    private String quizTitle;

    @Column(name = "correct_count", nullable = false)
    private int correct;

    @Column(name = "total_questions", nullable = false)
    private int total;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected QuizAttempt() {
    }

    public QuizAttempt(Student student, Quiz quiz, int correct, int total) {
        if (total < 1 || total > 100 || correct < 0 || correct > total) {
            throw new IllegalArgumentException("Invalid quiz score.");
        }
        this.student = student;
        this.quiz = quiz;
        this.quizTitle = quiz.getTitle();
        this.correct = correct;
        this.total = total;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public String getQuizTitle() { return quizTitle; }
    public int getCorrect() { return correct; }
    public int getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public int getScore() { return (int) Math.round(100.0 * correct / total); }
}
