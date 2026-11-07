package com.tutorly.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 60)
    private String subject;

    @Column(nullable = false, length = 120)
    private String topic;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY)
    @OrderBy("position ASC, id ASC")
    private List<QuizQuestion> questions = new ArrayList<>();

    protected Quiz() {
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getTopic() { return topic; }
    public List<QuizQuestion> getQuestions() { return questions; }
}
