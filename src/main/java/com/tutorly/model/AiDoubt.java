package com.tutorly.model;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name="ai_doubts",uniqueConstraints=@UniqueConstraint(name="uq_ai_student_request",columnNames={"student_id","request_id"}),
    indexes={@Index(name="idx_ai_student_day",columnList="student_id,request_day"),@Index(name="idx_ai_student_created",columnList="student_id,created_at,id")})
public class AiDoubt {
    public enum Status { PROCESSING, ANSWERED, FAILED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="student_id",nullable=false) private Student student;
    @Column(name="request_id",nullable=false,length=36) private String requestId;
    @Column(name="request_day",nullable=false) private LocalDate requestDay;
    @Column(nullable=false,length=60) private String subject;
    @Column(nullable=false,length=4000) private String question;
    @Column(nullable=false,length=30) private String grade;
    @Column(nullable=false,length=120) private String model;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(columnDefinition="TEXT") private String answer;
    @Column(name="error_code",length=40) private String errorCode;
    @Column(nullable=false) private boolean truncated;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="completed_at") private Instant completedAt;
    protected AiDoubt(){}
    public AiDoubt(Student student,String requestId,LocalDate day,String subject,String question,String model,Instant now){
        this.student=student;this.requestId=requestId;this.requestDay=day;this.subject=subject;this.question=question;
        this.grade=student.getGrade();this.model=model;this.createdAt=now;this.status=Status.PROCESSING;
    }
    public Long getId(){return id;} public String getRequestId(){return requestId;}
    public String getSubject(){return subject;} public String getQuestion(){return question;} public String getGrade(){return grade;}
    public String getModel(){return model;} public Status getStatus(){return status;} public String getAnswer(){return answer;}
    public String getErrorCode(){return errorCode;} public boolean isTruncated(){return truncated;}
    public Instant getCreatedAt(){return createdAt;} public Instant getCompletedAt(){return completedAt;}
    public void answer(String text,boolean truncated,Instant now){if(status==Status.PROCESSING){this.answer=text;this.truncated=truncated;status=Status.ANSWERED;completedAt=now;}}
    public void fail(String code,Instant now){if(status==Status.PROCESSING){status=Status.FAILED;errorCode=code;completedAt=now;}}
}
