package com.tutorly.controller;
import com.tutorly.ai.*;
import com.tutorly.dto.AiDoubtDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai/doubts")
public class AiDoubtController {
    private final AiDoubtService service;private final AiDoubtStore store;
    public AiDoubtController(AiDoubtService service,AiDoubtStore store){this.service=service;this.store=store;}
    @PostMapping public ResponseEntity<Detail> ask(Authentication auth,@Valid @RequestBody Ask input){var result=service.ask(id(auth),input);return ResponseEntity.status(result.status()).body(result.doubt());}
    @GetMapping public Results<Detail> history(Authentication auth,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return store.history(id(auth),page,size);}
    @GetMapping("/usage") public Usage usage(Authentication auth){return store.usage(id(auth));}
    @GetMapping("/{id}") public Detail get(Authentication auth,@PathVariable Long id){return store.get(id(auth),id);}
    private Long id(Authentication auth){
        if(auth==null||!auth.isAuthenticated())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"LOGIN_REQUIRED");
        if(auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_STUDENT")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"STUDENT_REQUIRED");
        try{long id=Long.parseLong(auth.getName());if(id>0)return id;}catch(NumberFormatException ignored){}
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"INVALID_LOGIN");
    }
}
