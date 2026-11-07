package com.tutorly.controller;

import com.tutorly.dto.SessionDtos.*;
import com.tutorly.service.SessionBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers/me")
public class TeacherSessionController {
    private final SessionBookingService service;
    public TeacherSessionController(SessionBookingService service){this.service=service;}
    @PostMapping("/slots") @ResponseStatus(HttpStatus.CREATED)
    public Slot create(Authentication a,@Valid @RequestBody CreateSlot r){return service.createSlot(SessionAccess.id(a,"TEACHER"),r);}
    @GetMapping("/slots")
    public Results<Slot> slots(Authentication a,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.mySlots(SessionAccess.id(a,"TEACHER"),page,size);}
    @PostMapping("/slots/{id}/cancel")
    public Slot cancelSlot(Authentication a,@PathVariable Long id){return service.cancelSlot(SessionAccess.id(a,"TEACHER"),id);}
    @GetMapping("/bookings")
    public Results<Booking> bookings(Authentication a,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.teacherBookings(SessionAccess.id(a,"TEACHER"),page,size);}
    @PostMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(Authentication a,@PathVariable Long id){return service.cancelByTeacher(SessionAccess.id(a,"TEACHER"),id);}
}
