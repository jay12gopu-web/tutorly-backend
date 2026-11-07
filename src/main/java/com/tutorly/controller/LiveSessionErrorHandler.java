package com.tutorly.controller;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes={TeacherMeetingController.class,StudentSessionJoinController.class})
public class LiveSessionErrorHandler {
    @ModelAttribute
    public void noCache(HttpServletResponse response){
        response.setHeader("Cache-Control","no-store");response.setHeader("Pragma","no-cache");
    }
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> locked(PessimisticLockingFailureException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Session is being updated. Refresh and retry."));
    }
}
