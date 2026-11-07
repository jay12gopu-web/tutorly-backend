package com.tutorly.controller;

import com.tutorly.dto.StudentRequest;
import com.tutorly.model.Student;
import com.tutorly.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> getAllStudents(
            Authentication authentication
    ) {
        Long id = Long.valueOf(authentication.getName());

        return List.of(service.getStudentById(id));
    }

    @GetMapping("/{id}")
    public Student getStudent(
            @PathVariable Long id,
            Authentication authentication
    ) {
        requireOwnProfile(id, authentication);
        return service.getStudentById(id);
    }

    @PutMapping("/{id}")
    public Student updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request,
            Authentication authentication
    ) {
        requireOwnProfile(id, authentication);
        return service.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        requireOwnProfile(id, authentication);
        service.deleteStudent(id);

        new SecurityContextLogoutHandler()
                .logout(request, response, authentication);
    }

    private void requireOwnProfile(
            Long id,
            Authentication authentication
    ) {
        if (!id.toString().equals(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Student not found."
            );
        }
    }
}