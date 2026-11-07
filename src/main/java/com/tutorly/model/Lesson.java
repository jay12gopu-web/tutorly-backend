package com.tutorly.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 60)
    private String subject;

    @Column(nullable = false, length = 120)
    private String topic;

    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @OrderBy("stepNumber ASC, id ASC")
    private List<LessonStep> steps = new ArrayList<>();

    protected Lesson() {
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public List<LessonStep> getSteps() { return steps; }
}
