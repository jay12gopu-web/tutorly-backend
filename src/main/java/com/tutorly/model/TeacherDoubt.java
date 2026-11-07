package com.tutorly.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="teacher_doubts", indexes={
    @Index(name="idx_doubt_student_updated",columnList="student_id,updated_at,id"),
    @Index(name="idx_doubt_teacher_updated",columnList="teacher_id,updated_at,id")})
public class TeacherDoubt {
    public enum Status { OPEN, RESOLVED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id",nullable=false) private Student student;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="teacher_id",nullable=false) private Teacher teacher;
    @Column(nullable=false,length=120) private String title;
    @Column(nullable=false,length=60) private String subject;
    @Column(nullable=false,length=4000) private String question;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Column(name="resolved_at") private Instant resolvedAt;
    protected TeacherDoubt() {}
    public TeacherDoubt(Student student,Teacher teacher,String title,String subject,String question,Instant now) {
        this.student=student;this.teacher=teacher;this.title=title;this.subject=subject;this.question=question;
        this.status=Status.OPEN;this.createdAt=now;this.updatedAt=now;
    }
    public Long getId(){return id;}
    public Student getStudent(){return student;}
    public Teacher getTeacher(){return teacher;}
    public String getTitle(){return title;}
    public String getSubject(){return subject;}
    public String getQuestion(){return question;}
    public Status getStatus(){return status;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
    public Instant getResolvedAt(){return resolvedAt;}
    public void touch(Instant now){updatedAt=now;}
    public void setResolved(boolean resolved,Instant now){
        Status next=resolved?Status.RESOLVED:Status.OPEN;
        if(status!=next){status=next;resolvedAt=resolved?now:null;updatedAt=now;}
    }
}
