package com.tutorly.controller;

import com.tutorly.auth.TeacherAuthService;
import com.tutorly.dto.TeacherDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher-auth")
public class TeacherAuthController {
    private final TeacherAuthService service;
    public TeacherAuthController(TeacherAuthService service) { this.service=service; }
    @PostMapping(value="/register",consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OwnProfile register(@Valid @RequestBody Register request) { return service.register(request); }
    @PostMapping(value="/login",consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Token> login(@Valid @RequestBody Login request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header(HttpHeaders.PRAGMA,"no-cache").body(service.login(request));
    }
}
