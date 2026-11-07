package com.tutorly.service;
import com.tutorly.dto.LiveSessionDtos.*;
import com.tutorly.model.*;
import com.tutorly.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.*;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(isolation=Isolation.READ_COMMITTED)
public class LiveSessionAccessService {
    public static final long JOIN_EARLY_MINUTES=15;
    private final LiveSessionMeetingRepository meetings;
    private final TutorSessionSlotRepository slots;
    private final SessionBookingRepository bookings;
    private final EntityManager em;
    private final Clock clock;
    @Autowired
    public LiveSessionAccessService(LiveSessionMeetingRepository meetings,TutorSessionSlotRepository slots,
                                    SessionBookingRepository bookings,EntityManager em){
        this(meetings,slots,bookings,em,Clock.systemUTC());
    }
    public LiveSessionAccessService(LiveSessionMeetingRepository meetings,TutorSessionSlotRepository slots,
                                    SessionBookingRepository bookings,EntityManager em,Clock clock){
        this.meetings=meetings;this.slots=slots;this.bookings=bookings;this.em=em;this.clock=clock;
    }
    public Meeting get(Long teacherId,Long slotId){
        ownSlot(teacherId,slotId);
        return meetings.findById(slotId).map(m->view(slotId,m)).orElse(new Meeting(slotId,false,null,null));
    }
    public Meeting put(Long teacherId,Long slotId,MeetingInput input){
        var slot=ownSlot(teacherId,slotId);
        if(slot.isCancelled())throw conflict("Cancelled slots cannot receive a meeting link");
        if(slot.getDeliveryMode()!=TutorSessionSlot.DeliveryMode.ONLINE)throw conflict("Meeting links are only for ONLINE slots");
        Instant now=clock.instant().truncatedTo(ChronoUnit.MICROS);
        if(!now.isBefore(slot.getEndsAt()))throw conflict("Session has ended");
        String url=MeetingUrlValidator.validate(input.meetingUrl());
        var meeting=meetings.findById(slotId).orElseGet(()->new LiveSessionMeeting(slot,url,now));
        meeting.update(url,now);
        return view(slotId,meetings.saveAndFlush(meeting));
    }
    public void remove(Long teacherId,Long slotId){
        ownSlot(teacherId,slotId);
        meetings.findById(slotId).ifPresent(m->{meetings.delete(m);meetings.flush();});
    }
    public JoinAccess join(Long studentId,Long bookingId){
        positive(bookingId);
        Long teacherId=meetings.teacherForStudentBooking(bookingId,studentId).orElseThrow(LiveSessionAccessService::missing);
        // Same first lock as booking/cancellation code: cancellation and link reads serialize.
        lockTeacher(teacherId);
        var booking=bookings.lockById(bookingId).orElseThrow(LiveSessionAccessService::missing);
        if(!booking.getStudent().getId().equals(studentId))throw missing();
        var slot=booking.getSlot();
        Instant now=clock.instant(),opens=slot.getStartsAt().minus(JOIN_EARLY_MINUTES,ChronoUnit.MINUTES),closes=slot.getEndsAt();
        JoinState state;
        String url=null;
        if(booking.getStatus()!=SessionBooking.Status.CONFIRMED||slot.isCancelled())state=JoinState.CANCELLED;
        else if(slot.getDeliveryMode()!=TutorSessionSlot.DeliveryMode.ONLINE)state=JoinState.IN_PERSON;
        else if(!now.isBefore(closes))state=JoinState.ENDED;
        else if(now.isBefore(opens))state=JoinState.TOO_EARLY;
        else {
            var meeting=meetings.findById(slot.getId());
            if(meeting.isEmpty())state=JoinState.NOT_CONFIGURED;
            else {state=JoinState.READY;url=meeting.get().getMeetingUrl();}
        }
        return new JoinAccess(bookingId,slot.getId(),state,opens,closes,url);
    }
    private TutorSessionSlot ownSlot(Long teacherId,Long slotId){
        positive(slotId);
        if(!slots.teacherId(slotId).orElseThrow(LiveSessionAccessService::missing).equals(teacherId))throw missing();
        lockTeacher(teacherId);
        return slots.lockById(slotId).orElseThrow(LiveSessionAccessService::missing);
    }
    private void lockTeacher(Long id){if(em.find(Teacher.class,id,LockModeType.PESSIMISTIC_WRITE)==null)throw missing();}
    private static Meeting view(Long id,LiveSessionMeeting m){return new Meeting(id,true,m.getMeetingUrl(),m.getUpdatedAt());}
    private static void positive(Long id){if(id==null||id<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID must be positive");}
    private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Session or booking not found");}
    private static ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
}
