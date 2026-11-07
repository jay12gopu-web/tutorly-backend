package com.tutorly.controller;
import com.tutorly.dto.LiveSessionDtos.JoinAccess;
import com.tutorly.service.LiveSessionAccessService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/bookings")
public class StudentSessionJoinController {
    private final LiveSessionAccessService service;
    public StudentSessionJoinController(LiveSessionAccessService service){this.service=service;}
    @GetMapping("/{bookingId}/join")
    public JoinAccess join(Authentication auth,@PathVariable Long bookingId){return service.join(LiveSessionAccess.id(auth,"STUDENT"),bookingId);}
}
