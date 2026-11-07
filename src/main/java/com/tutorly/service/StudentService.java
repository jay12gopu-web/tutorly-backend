package com.tutorly.service;

import com.tutorly.dto.StudentRequest;
import com.tutorly.model.Student;
import com.tutorly.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Student createStudent(StudentRequest request) {
        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw duplicateEmail();
        }

        Student student = new Student();
        copyDetails(student, request, email);

        return saveStudent(student);
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student not found"
                ));
    }

    public Student updateStudent(Long id, StudentRequest request) {
        Student student = getStudentById(id);
        String email = normalizeEmail(request.email());

        if (repository.existsByEmailAndIdNot(email, id)) {
            throw duplicateEmail();
        }

        copyDetails(student, request, email);

        return saveStudent(student);
    }

    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        repository.delete(student);
    }

    private void copyDetails(
            Student student,
            StudentRequest request,
            String email
    ) {
        student.setName(request.name().strip());
        student.setEmail(email);
        student.setGrade(request.grade().strip());
        student.setCity(request.city().strip());
    }

    private Student saveStudent(Student student) {
        try {
            return repository.saveAndFlush(student);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Student details conflict with an existing record",
                    exception
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException duplicateEmail() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A student with this email already exists"
        );
    }
}