package com.tutorly.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "study_plans", indexes = {
        @Index(name = "idx_study_plans_student_start", columnList = "student_id,starts_at")
})
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_study_plans_student"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 60)
    private String subject;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private boolean done;

    protected StudyPlan() {
    }

    public StudyPlan(Student student, String title, String subject,
                     Instant startsAt, int durationMinutes) {
        this.student = student;
        this.title = title;
        this.subject = subject;
        this.startsAt = startsAt;
        this.durationMinutes = durationMinutes;
        this.done = false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public Instant getStartsAt() { return startsAt; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
