package com.tutorly.controller;

import com.tutorly.dto.QuizDtos.*;
import com.tutorly.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    @GetMapping("/quizzes")
    public List<Summary> list(Authentication authentication) {
        return service.list(studentId(authentication));
    }

    @GetMapping("/quizzes/{id}")
    public Detail get(Authentication authentication, @PathVariable Long id) {
        return service.get(studentId(authentication), id);
    }

    @PostMapping("/quizzes/{id}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public Attempt submit(Authentication authentication, @PathVariable Long id,
                           @Valid @RequestBody Submission request) {
        return service.submit(studentId(authentication), id, request);
    }

    @GetMapping("/quiz-attempts")
    public List<Attempt> history(Authentication authentication) {
        return service.history(studentId(authentication));
    }

    @GetMapping("/quiz-attempts/{id}")
    public Attempt getAttempt(Authentication authentication, @PathVariable Long id) {
        return service.getAttempt(studentId(authentication), id);
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
