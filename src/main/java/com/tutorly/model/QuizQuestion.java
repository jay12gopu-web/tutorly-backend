package com.tutorly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Table(name = "quiz_questions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_quiz_question_position", columnNames = {"quiz_id", "question_order"})
})
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_quiz_questions_quiz"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Quiz quiz;

    @Column(name = "question_order", nullable = false)
    private int position;

    @Column(nullable = false, length = 1000)
    private String prompt;

    @Column(name = "option_a", nullable = false, length = 300)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 300)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 300)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 300)
    private String optionD;

    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    protected QuizQuestion() {
    }

    public Long getId() { return id; }
    public String getPrompt() { return prompt; }

    public List<String> getOptions() {
        return List.of(optionA, optionB, optionC, optionD);
    }

    @JsonIgnore
    public int getCorrectOption() { return correctOption; }
}
