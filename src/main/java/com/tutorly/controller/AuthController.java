package com.tutorly.controller;

import com.tutorly.auth.JwtAuthService;
import com.tutorly.dto.JwtAuthDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtAuthService service;
    public AuthController(JwtAuthService service) { this.service = service; }

    @PostMapping(value="/register", consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Profile register(@Valid @RequestBody Register request) { return service.register(request); }

    @PostMapping(value="/login", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Token> login(@Valid @RequestBody Login request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache").body(service.login(request));
    }

    @GetMapping("/me")
    public Profile me(Authentication authentication) {
        return service.me(Long.valueOf(authentication.getName()));
    }
}
