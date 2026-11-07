package com.tutorly.controller;

import com.tutorly.dto.LessonDtos.*;
import com.tutorly.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @GetMapping("/lessons")
    public List<Summary> list(Authentication authentication) {
        return service.list(studentId(authentication));
    }

    @GetMapping("/lessons/{id}")
    public Detail get(Authentication authentication, @PathVariable Long id) {
        return service.get(studentId(authentication), id);
    }

    @GetMapping("/lessons/{id}/progress")
    public Progress getProgress(Authentication authentication, @PathVariable Long id) {
        return service.getProgress(studentId(authentication), id);
    }

    @PutMapping("/lessons/{id}/progress")
    public Progress updateProgress(Authentication authentication, @PathVariable Long id,
                                    @Valid @RequestBody UpdateProgress request) {
        return service.updateProgress(studentId(authentication), id, request);
    }

    @GetMapping("/lesson-progress")
    public List<Progress> history(Authentication authentication) {
        return service.history(studentId(authentication));
    }

    private Long studentId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in.");
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Please log in using the student authentication endpoints.");
        }
    }
}
