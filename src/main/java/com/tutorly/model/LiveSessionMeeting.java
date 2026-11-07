package com.tutorly.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="live_session_meetings")
public class LiveSessionMeeting {
    @Id @Column(name="slot_id") private Long slotId;
    @MapsId @OneToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="slot_id",nullable=false) private TutorSessionSlot slot;
    @Column(name="meeting_url",nullable=false,length=2048) private String meetingUrl;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected LiveSessionMeeting() {}
    public LiveSessionMeeting(TutorSessionSlot slot,String meetingUrl,Instant now){
        this.slot=slot;this.meetingUrl=meetingUrl;this.updatedAt=now;
    }
    public Long getSlotId(){return slotId;}
    public String getMeetingUrl(){return meetingUrl;}
    public Instant getUpdatedAt(){return updatedAt;}
    public void update(String meetingUrl,Instant now){this.meetingUrl=meetingUrl;this.updatedAt=now;}
}
