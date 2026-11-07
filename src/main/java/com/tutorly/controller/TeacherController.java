package com.tutorly.controller;

import com.tutorly.dto.TeacherDtos.*;
import com.tutorly.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherService service;
    public TeacherController(TeacherService service) { this.service=service; }
    @GetMapping
    public SearchResult search(@RequestParam(defaultValue="") String city,
                               @RequestParam(defaultValue="") String area,
                               @RequestParam(defaultValue="") String subject,
                               @RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="20") int size) {
        return service.search(city,area,subject,page,size);
    }
    @GetMapping("/{id}")
    public PublicProfile get(@PathVariable Long id) { return service.getListed(id); }
    @GetMapping("/me")
    public ResponseEntity<OwnProfile> me(Authentication auth) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.me(teacherId(auth)));
    }
    @PutMapping("/me")
    public ResponseEntity<OwnProfile> update(Authentication auth,@Valid @RequestBody Update request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.update(teacherId(auth),request));
    }
    private Long teacherId(Authentication auth) {
        if(auth==null || !auth.isAuthenticated())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please log in");
        if(auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_TEACHER")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Teacher login required");
        try { long id=Long.parseLong(auth.getName());if(id>0)return id; }
        catch(NumberFormatException ignored) {}
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid teacher login");
    }
}
