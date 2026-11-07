package com.tutorly.controller;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes={TeacherLocationController.class,NearbyTeacherController.class})
public class NearbyTeacherErrorHandler {
    @ModelAttribute public void noCache(HttpServletResponse response){response.setHeader("Cache-Control","no-store");}
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String,String>> locked(PessimisticLockingFailureException ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Location is being updated. Refresh and retry."));}
}
