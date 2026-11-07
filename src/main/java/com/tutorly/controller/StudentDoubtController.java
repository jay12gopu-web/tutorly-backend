package com.tutorly.controller;
import com.tutorly.dto.DoubtDtos.*;
import com.tutorly.service.TeacherDoubtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doubts")
public class StudentDoubtController {
    private final TeacherDoubtService service;
    public StudentDoubtController(TeacherDoubtService service){this.service=service;}
    @GetMapping
    public Results<Summary> list(Authentication auth,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        return service.list(DoubtAccess.id(auth,"STUDENT"),false,page,size);
    }
    @GetMapping("/{id}")
    public Detail get(Authentication auth,@PathVariable Long id){return service.get(DoubtAccess.id(auth,"STUDENT"),false,id);}
    @GetMapping("/{id}/replies")
    public Results<Reply> replies(Authentication auth,@PathVariable Long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){
        return service.replies(DoubtAccess.id(auth,"STUDENT"),false,id,page,size);
    }
    @PostMapping("/{id}/replies") @ResponseStatus(HttpStatus.CREATED)
    public Reply reply(Authentication auth,@PathVariable Long id,@Valid @RequestBody ReplyInput input){
        return service.reply(DoubtAccess.id(auth,"STUDENT"),false,id,input);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Detail create(Authentication auth,@Valid @RequestBody Create input){return service.create(DoubtAccess.id(auth,"STUDENT"),input);}
    @PostMapping("/{id}/resolve")
    public Detail resolve(Authentication auth,@PathVariable Long id){return service.resolve(DoubtAccess.id(auth,"STUDENT"),id,true);}
    @PostMapping("/{id}/reopen")
    public Detail reopen(Authentication auth,@PathVariable Long id){return service.resolve(DoubtAccess.id(auth,"STUDENT"),id,false);}

}
