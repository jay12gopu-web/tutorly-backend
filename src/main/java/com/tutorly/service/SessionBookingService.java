package com.tutorly.service;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.dto.SessionDtos.*;
import com.tutorly.model.*;
import com.tutorly.model.SessionBooking.Status;
import com.tutorly.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class SessionBookingService {
    private final TutorSessionSlotRepository slots;
    private final SessionBookingRepository bookings;
    private final TeacherRepository teachers;
    private final AuthStudentRepository students;
    private final EntityManager em;
    private final Clock clock;
    @Autowired
    public SessionBookingService(TutorSessionSlotRepository slots,SessionBookingRepository bookings,
                                 TeacherRepository teachers,AuthStudentRepository students,EntityManager em) {
        this(slots,bookings,teachers,students,em,Clock.systemUTC());
    }
    public SessionBookingService(TutorSessionSlotRepository slots,SessionBookingRepository bookings,
                                 TeacherRepository teachers,AuthStudentRepository students,EntityManager em,Clock clock) {
        this.slots=slots;this.bookings=bookings;this.teachers=teachers;this.students=students;this.em=em;this.clock=clock;
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Slot createSlot(Long teacherId,CreateSlot r) {
        Teacher teacher=lockTeacher(teacherId);
        Instant now=clock.instant();
        if(r.startsAt()==null || !r.startsAt().isAfter(now) || r.startsAt().isAfter(now.plus(365,ChronoUnit.DAYS)))
            throw bad("Choose a future start within the next 365 days");
        if(r.durationMinutes()==null || r.durationMinutes()<15 || r.durationMinutes()>180)throw bad("Duration must be 15–180 minutes");
        String subject=teacher.getSubjects().stream().filter(s->s.equalsIgnoreCase(r.subject().strip())).findFirst()
                .orElseThrow(()->bad("Choose a subject from your teacher profile"));
        if(teacher.getTeachingMode()!=Teacher.TeachingMode.BOTH && !teacher.getTeachingMode().name().equals(r.deliveryMode().name()))
            throw bad("Delivery mode must match your teacher profile");
        Instant end=r.startsAt().plus(r.durationMinutes(),ChronoUnit.MINUTES);
        if(slots.countOverlaps(teacherId,r.startsAt(),end)>0)throw conflict("This time overlaps another active teacher slot");
        var slot=new TutorSessionSlot(teacher,r.title().strip(),subject,r.startsAt(),end,r.sessionType(),r.deliveryMode());
        return Slot.from(slots.saveAndFlush(slot),false);
    }

    public Results<Slot> mySlots(Long teacherId,int page,int size) {
        requireTeacher(teacherId);
        return Results.from(slots.findByTeacher_Id(teacherId,page(page,size,"startsAt",false)).map(s->
                Slot.from(s,bookings.existsBySlot_IdAndStatus(s.getId(),Status.CONFIRMED))));
    }
    public Results<Slot> available(Long studentId,Long teacherId,int page,int size) {
        requireStudent(studentId);
        if(teachers.findByIdAndListedTrue(teacherId).isEmpty())throw missing();
        return Results.from(slots.available(teacherId,clock.instant(),Status.CONFIRMED,page(page,size,"startsAt",false))
                .map(s->Slot.from(s,false)));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Booking book(Long studentId,Long slotId) {
        // Read just the owner ID first; load the slot only AFTER acquiring the owner lock.
        Long teacherId=slots.teacherId(slotId).orElseThrow(SessionBookingService::missing);
        Teacher teacher=lockTeacher(teacherId);
        Student student=em.find(Student.class,studentId,LockModeType.PESSIMISTIC_WRITE);
        if(student==null)throw unauthenticated();
        TutorSessionSlot slot=slots.lockById(slotId).orElseThrow(SessionBookingService::missing);
        Instant now=clock.instant();
        if(!teacher.isListed())throw missing();
        if(slot.isCancelled() || !slot.getStartsAt().isAfter(now))throw conflict("Slot is cancelled or has already started");
        if(bookings.existsBySlot_IdAndStatus(slotId,Status.CONFIRMED))throw conflict("Slot is already booked");
        if(bookings.countStudentOverlaps(studentId,slot.getStartsAt(),slot.getEndsAt(),Status.CONFIRMED)>0)
            throw conflict("You already have a booking during this time");
        try { return Booking.from(bookings.saveAndFlush(new SessionBooking(slot,student,now))); }
        catch(DataIntegrityViolationException ex) { throw conflict("Slot could not be reserved; refresh availability and retry"); }
    }

    public Results<Booking> studentBookings(Long studentId,int page,int size) {
        requireStudent(studentId);
        return Results.from(bookings.findByStudent_Id(studentId,page(page,size,"createdAt",true)).map(Booking::from));
    }
    public Results<Booking> teacherBookings(Long teacherId,int page,int size) {
        requireTeacher(teacherId);
        return Results.from(bookings.findBySlot_Teacher_Id(teacherId,page(page,size,"createdAt",true)).map(Booking::from));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Booking cancelByStudent(Long studentId,Long bookingId) {
        Long teacherId=bookings.teacherId(bookingId).orElseThrow(SessionBookingService::missing);
        lockTeacher(teacherId);
        if(em.find(Student.class,studentId,LockModeType.PESSIMISTIC_WRITE)==null)throw unauthenticated();
        var booking=bookings.lockById(bookingId).orElseThrow(SessionBookingService::missing);
        if(!booking.getStudent().getId().equals(studentId))throw missing();
        return cancelBooking(booking,Status.CANCELLED_BY_STUDENT);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Booking cancelByTeacher(Long teacherId,Long bookingId) {
        if(!bookings.teacherId(bookingId).orElseThrow(SessionBookingService::missing).equals(teacherId))throw missing();
        lockTeacher(teacherId);
        var booking=bookings.lockById(bookingId).orElseThrow(SessionBookingService::missing);
        return cancelBooking(booking,Status.CANCELLED_BY_TEACHER);
    }
    private Booking cancelBooking(SessionBooking booking,Status status) {
        if(booking.getStatus()!=Status.CONFIRMED)return Booking.from(booking);
        Instant now=clock.instant();
        if(!booking.getSlot().getStartsAt().isAfter(now))throw conflict("Started sessions cannot be cancelled");
        booking.cancel(status,now);
        return Booking.from(bookings.saveAndFlush(booking));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Slot cancelSlot(Long teacherId,Long slotId) {
        if(!slots.teacherId(slotId).orElseThrow(SessionBookingService::missing).equals(teacherId))throw missing();
        lockTeacher(teacherId);
        var slot=slots.lockById(slotId).orElseThrow(SessionBookingService::missing);
        if(slot.isCancelled())return Slot.from(slot,false);
        if(!slot.getStartsAt().isAfter(clock.instant()))throw conflict("Started slots cannot be cancelled");
        bookings.findBySlot_IdAndStatus(slotId,Status.CONFIRMED).ifPresent(b->{
            b.cancel(Status.CANCELLED_BY_TEACHER,clock.instant());bookings.saveAndFlush(b);
        });
        slot.cancel();
        return Slot.from(slots.saveAndFlush(slot),false);
    }

    private Teacher lockTeacher(Long id) {
        Teacher teacher=em.find(Teacher.class,id,LockModeType.PESSIMISTIC_WRITE);
        if(teacher==null)throw missing();
        return teacher;
    }
    private void requireTeacher(Long id){if(!teachers.existsById(id))throw unauthenticated();}
    private void requireStudent(Long id){if(!students.existsById(id))throw unauthenticated();}
    private Pageable page(int page,int size,String field,boolean descending) {
        if(page<0 || page>10000 || size<1 || size>50)throw bad("Use page 0–10000 and size 1–50");
        Sort sort=Sort.by(field,"id");return PageRequest.of(page,size,descending?sort.descending():sort.ascending());
    }
    private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Session or booking not found");}
    private static ResponseStatusException unauthenticated(){return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account no longer exists");}
    private static ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private static ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
}
