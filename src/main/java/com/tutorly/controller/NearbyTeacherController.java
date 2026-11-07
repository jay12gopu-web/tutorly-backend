package com.tutorly.controller;
import com.tutorly.dto.NearbyTeacherDtos.Results;
import com.tutorly.service.NearbyTeacherService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/teachers/nearby")
public class NearbyTeacherController {
    private final NearbyTeacherService service;
    public NearbyTeacherController(NearbyTeacherService service){this.service=service;}
    @GetMapping public Results search(Authentication auth,@RequestParam Double latitude,@RequestParam Double longitude,
        @RequestParam(defaultValue="10") Double radiusKm,@RequestParam(defaultValue="") String subject,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        NearbyTeacherAccess.id(auth,"STUDENT");return service.search(latitude,longitude,radiusKm,subject,page,size);
    }
}
