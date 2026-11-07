package com.tutorly.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="session_bookings",uniqueConstraints=@UniqueConstraint(name="uq_booking_active_slot",columnNames="active_slot_id"),
        indexes={@Index(name="idx_booking_student_status",columnList="student_id,status"),
                 @Index(name="idx_booking_slot_status",columnList="slot_id,status")})
public class SessionBooking {
    public enum Status { CONFIRMED, CANCELLED_BY_STUDENT, CANCELLED_BY_TEACHER }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="slot_id",nullable=false) private TutorSessionSlot slot;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="student_id",nullable=false) private Student student;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Status status;
    @Column(name="active_slot_id") private Long activeSlotId;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="cancelled_at") private Instant cancelledAt;
    protected SessionBooking() {}
    public SessionBooking(TutorSessionSlot slot,Student student,Instant now) {
        this.slot=slot;this.student=student;this.status=Status.CONFIRMED;
        this.activeSlotId=slot.getId();this.createdAt=now;
    }
    public void cancel(Status cancelledBy,Instant now) {
        if(cancelledBy==Status.CONFIRMED)throw new IllegalArgumentException("Cancellation status required");
        if(status==Status.CONFIRMED){status=cancelledBy;activeSlotId=null;cancelledAt=now;}
    }
    public Long getId(){return id;} public TutorSessionSlot getSlot(){return slot;}
    public Student getStudent(){return student;} public Status getStatus(){return status;}
    public Long getActiveSlotId(){return activeSlotId;}
    public Instant getCreatedAt(){return createdAt;} public Instant getCancelledAt(){return cancelledAt;}
}
