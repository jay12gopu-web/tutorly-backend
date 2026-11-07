package com.tutorly.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "lesson_steps", uniqueConstraints = {
        @UniqueConstraint(name = "uq_lesson_step_number", columnNames = {"lesson_id", "step_number"})
})
public class LessonStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lesson_steps_lesson"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lesson lesson;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(nullable = false, length = 160)
    private String heading;

    @Column(name = "body_text", nullable = false, length = 4000)
    private String body;

    @Column(name = "diagram_mermaid", length = 2000)
    private String diagramMermaid;

    protected LessonStep() {
    }

    public Long getId() { return id; }
    public int getStepNumber() { return stepNumber; }
    public String getHeading() { return heading; }
    public String getBody() { return body; }
    public String getDiagramMermaid() { return diagramMermaid; }
}
