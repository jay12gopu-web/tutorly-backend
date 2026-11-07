package com.tutorly.controller;
import java.util.Map;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes={StudentDoubtController.class,TeacherDoubtController.class})
public class DoubtErrorHandler {
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> locked(PessimisticLockingFailureException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Conversation is being updated. Refresh and retry."));
    }
}
