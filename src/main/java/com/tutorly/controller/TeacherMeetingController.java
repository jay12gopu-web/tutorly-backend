package com.tutorly.controller;
import com.tutorly.dto.LiveSessionDtos.*;
import com.tutorly.service.LiveSessionAccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/teachers/me/slots/{slotId}/meeting")
public class TeacherMeetingController {
    private final LiveSessionAccessService service;
    public TeacherMeetingController(LiveSessionAccessService service){this.service=service;}
    @GetMapping
    public Meeting get(Authentication auth,@PathVariable Long slotId){return service.get(LiveSessionAccess.id(auth,"TEACHER"),slotId);}
    @PutMapping
    public Meeting put(Authentication auth,@PathVariable Long slotId,@Valid @RequestBody MeetingInput input){return service.put(LiveSessionAccess.id(auth,"TEACHER"),slotId,input);}
    @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication auth,@PathVariable Long slotId){service.remove(LiveSessionAccess.id(auth,"TEACHER"),slotId);}
}
