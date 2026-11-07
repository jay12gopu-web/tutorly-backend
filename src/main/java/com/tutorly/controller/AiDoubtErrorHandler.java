package com.tutorly.controller;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice(assignableTypes=AiDoubtController.class)
public class AiDoubtErrorHandler {
    @ModelAttribute public void noCache(HttpServletResponse r){r.setHeader("Cache-Control","no-store");}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException ex){return ResponseEntity.status(ex.getStatusCode()).body(Map.of("code",ex.getReason()==null?"REQUEST_FAILED":ex.getReason()));}
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> locked(PessimisticLockingFailureException ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code","REQUEST_BUSY"));}
}
