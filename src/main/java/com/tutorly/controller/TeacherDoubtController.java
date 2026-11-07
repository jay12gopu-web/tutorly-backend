package com.tutorly.controller;
import com.tutorly.dto.DoubtDtos.*;
import com.tutorly.service.TeacherDoubtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers/me/doubts")
public class TeacherDoubtController {
    private final TeacherDoubtService service;
    public TeacherDoubtController(TeacherDoubtService service){this.service=service;}
    @GetMapping
    public Results<Summary> list(Authentication auth,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        return service.list(DoubtAccess.id(auth,"TEACHER"),true,page,size);
    }
    @GetMapping("/{id}")
    public Detail get(Authentication auth,@PathVariable Long id){return service.get(DoubtAccess.id(auth,"TEACHER"),true,id);}
    @GetMapping("/{id}/replies")
    public Results<Reply> replies(Authentication auth,@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        return service.replies(DoubtAccess.id(auth,"TEACHER"),true,id,page,size);
    }
    @PostMapping("/{id}/replies") @ResponseStatus(HttpStatus.CREATED)
    public Reply reply(Authentication auth,@PathVariable Long id,@Valid @RequestBody ReplyInput input){
        return service.reply(DoubtAccess.id(auth,"TEACHER"),true,id,input);
    }

}
