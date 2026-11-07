package com.tutorly.controller;

import java.util.Map;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes={TeacherSessionController.class,StudentBookingController.class})
public class SessionErrorHandler {
    @ExceptionHandler(PessimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,String> busy(PessimisticLockingFailureException ex) {
        return Map.of("error","Session is being changed. Refresh and retry.");
    }
}
