package com.tutorly.controller;
import com.tutorly.dto.NearbyTeacherDtos.*;
import com.tutorly.service.NearbyTeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/teachers/me/location")
public class TeacherLocationController {
    private final NearbyTeacherService service;
    public TeacherLocationController(NearbyTeacherService service){this.service=service;}
    @GetMapping public OwnLocation get(Authentication auth){return service.get(NearbyTeacherAccess.id(auth,"TEACHER"));}
    @PutMapping public OwnLocation put(Authentication auth,@Valid @RequestBody LocationInput input){return service.put(NearbyTeacherAccess.id(auth,"TEACHER"),input);}
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication auth){service.remove(NearbyTeacherAccess.id(auth,"TEACHER"));}
}
