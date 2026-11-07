package com.tutorly.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uq_lesson_progress_student_lesson", columnNames = {"student_id", "lesson_id"})
}, indexes = {
        @Index(name = "idx_lesson_progress_student_updated", columnList = "student_id,updated_at")
})
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lesson_progress_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lesson_progress_lesson"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lesson lesson;

    @Column(name = "completed_steps", nullable = false)
    private int completedSteps;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LessonProgress() {
    }

    public LessonProgress(Student student, Lesson lesson) {
        this.student = student;
        this.lesson = lesson;
        this.completedSteps = 0;
        this.updatedAt = Instant.now();
    }

    public void completeThrough(int completedSteps) {
        if (completedSteps > this.completedSteps) {
            this.completedSteps = completedSteps;
            this.updatedAt = Instant.now();
        }
    }

    public Lesson getLesson() { return lesson; }
    public int getCompletedSteps() { return completedSteps; }
    public Instant getUpdatedAt() { return updatedAt; }
}
