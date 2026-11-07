package com.tutorly.controller;

import com.tutorly.dto.StudyPlanDoneRequest;
import com.tutorly.dto.StudyPlanRequest;
import com.tutorly.dto.StudyPlanResponse;
import com.tutorly.service.StudyPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class StudyPlanController {

    private final StudyPlanService service;

    public StudyPlanController(StudyPlanService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyPlanResponse create(Authentication authentication,
                                    @Valid @RequestBody StudyPlanRequest request) {
        return service.create(studentId(authentication), request);
    }

    @GetMapping
    public List<StudyPlanResponse> getAll(Authentication authentication) {
        return service.getAll(studentId(authentication));
    }

    @GetMapping("/{id}")
    public StudyPlanResponse getOne(Authentication authentication,
                                    @PathVariable Long id) {
        return service.getOne(studentId(authentication), id);
    }

    @PatchMapping("/{id}")
    public StudyPlanResponse setDone(Authentication authentication,
                                     @PathVariable Long id,
                                     @Valid @RequestBody StudyPlanDoneRequest request) {
        return service.setDone(studentId(authentication), id, request.done());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.delete(studentId(authentication), id);
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
