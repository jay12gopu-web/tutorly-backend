package com.tutorly.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="tutor_session_slots", indexes=@Index(name="idx_slot_teacher_start",columnList="teacher_id,starts_at"))
public class TutorSessionSlot {
    public enum SessionType { LIVE, DEMO }
    public enum DeliveryMode { ONLINE, IN_PERSON }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="teacher_id",nullable=false) private Teacher teacher;
    @Column(nullable=false,length=120) private String title;
    @Column(nullable=false,length=60) private String subject;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Enumerated(EnumType.STRING) @Column(name="session_type",nullable=false,length=10) private SessionType sessionType;
    @Enumerated(EnumType.STRING) @Column(name="delivery_mode",nullable=false,length=15) private DeliveryMode deliveryMode;
    @Column(nullable=false) private boolean cancelled;
    protected TutorSessionSlot() {}
    public TutorSessionSlot(Teacher teacher,String title,String subject,Instant startsAt,Instant endsAt,
                            SessionType sessionType,DeliveryMode deliveryMode) {
        this.teacher=teacher;this.title=title;this.subject=subject;this.startsAt=startsAt;this.endsAt=endsAt;
        this.sessionType=sessionType;this.deliveryMode=deliveryMode;
    }
    public Long getId(){return id;} public Teacher getTeacher(){return teacher;}
    public String getTitle(){return title;} public String getSubject(){return subject;}
    public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;}
    public SessionType getSessionType(){return sessionType;} public DeliveryMode getDeliveryMode(){return deliveryMode;}
    public boolean isCancelled(){return cancelled;} public void cancel(){cancelled=true;}
}
