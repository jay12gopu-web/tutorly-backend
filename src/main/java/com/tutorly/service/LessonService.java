package com.tutorly.service;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.dto.LessonDtos.*;
import com.tutorly.model.Lesson;
import com.tutorly.model.LessonProgress;
import com.tutorly.model.Student;
import com.tutorly.repository.LessonProgressRepository;
import com.tutorly.repository.LessonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final AuthStudentRepository students;
    private final EntityManager entityManager;

    public LessonService(LessonRepository lessons, LessonProgressRepository progress,
                         AuthStudentRepository students, EntityManager entityManager) {
        this.lessons = lessons;
        this.progress = progress;
        this.students = students;
        this.entityManager = entityManager;
    }

    public List<Summary> list(Long studentId) {
        requireStudent(studentId);
        return lessons.findAllByOrderByIdAsc().stream().map(Summary::from).toList();
    }

    public Detail get(Long studentId, Long lessonId) {
        requireStudent(studentId);
        return Detail.from(requireLesson(lessonId));
    }

    public Progress getProgress(Long studentId, Long lessonId) {
        requireStudent(studentId);
        Lesson lesson = requireLesson(lessonId);
        LessonProgress saved = progress.findByStudent_IdAndLesson_Id(studentId, lessonId)
                .orElse(null);
        return Progress.from(lesson, saved);
    }

    public List<Progress> history(Long studentId) {
        requireStudent(studentId);
        return progress.findByStudent_IdOrderByUpdatedAtDescIdDesc(studentId)
                .stream().distinct()
                .map(saved -> Progress.from(saved.getLesson(), saved)).toList();
    }

    @Transactional
    public Progress updateProgress(Long studentId, Long lessonId, UpdateProgress request) {
        // Lock the existing student row before checking/creating progress.
        // This also serializes two first-time updates for the same student.
        Student student = entityManager.find(Student.class, studentId, LockModeType.PESSIMISTIC_WRITE);
        if (student == null) throw missingStudent();

        Lesson lesson = requireLesson(lessonId);
        int total = lesson.getSteps().size();
        if (total == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This lesson has no steps yet.");
        }
        if (request.completedSteps() == null || request.completedSteps() < 1
                || request.completedSteps() > total) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "completedSteps must be between 1 and " + total + ".");
        }

        LessonProgress saved = progress.findByStudent_IdAndLesson_Id(studentId, lessonId)
                .orElseGet(() -> new LessonProgress(student, lesson));

        if (request.completedSteps() > saved.getCompletedSteps()) {
            saved.completeThrough(request.completedSteps());
            saved = progress.save(saved);
        }
        return Progress.from(lesson, saved);
    }

    private Lesson requireLesson(Long lessonId) {
        return lessons.findWithStepsById(lessonId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found."));
    }

    private void requireStudent(Long studentId) {
        if (!students.existsById(studentId)) throw missingStudent();
    }

    private ResponseStatusException missingStudent() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Your student account is no longer available. Log in again.");
    }
}
