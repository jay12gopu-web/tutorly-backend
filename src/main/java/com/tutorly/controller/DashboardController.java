package com.tutorly.controller;

import com.tutorly.dto.DashboardResponse;
import com.tutorly.service.DashboardService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<DashboardResponse> get(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) throw loginRequired();
        final long studentId;
        try { studentId = Long.parseLong(authentication.getName()); }
        catch (NumberFormatException ex) { throw loginRequired(); }
        if (studentId < 1) throw loginRequired();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.get(studentId));
    }

    private ResponseStatusException loginRequired() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
    }
}
