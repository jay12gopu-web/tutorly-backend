package com.tutorly.controller;

import com.tutorly.dto.SessionDtos.*;
import com.tutorly.service.SessionBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StudentBookingController {
    private final SessionBookingService service;
    public StudentBookingController(SessionBookingService service){this.service=service;}
    @GetMapping("/teachers/{teacherId}/slots")
    public Results<Slot> available(Authentication a,@PathVariable Long teacherId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.available(SessionAccess.id(a,"STUDENT"),teacherId,page,size);}
    @PostMapping("/bookings") @ResponseStatus(HttpStatus.CREATED)
    public Booking book(Authentication a,@Valid @RequestBody Book r){return service.book(SessionAccess.id(a,"STUDENT"),r.slotId());}
    @GetMapping("/bookings")
    public Results<Booking> bookings(Authentication a,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.studentBookings(SessionAccess.id(a,"STUDENT"),page,size);}
    @PostMapping("/bookings/{id}/cancel")
    public Booking cancel(Authentication a,@PathVariable Long id){return service.cancelByStudent(SessionAccess.id(a,"STUDENT"),id);}
}
