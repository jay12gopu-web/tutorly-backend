package com.tutorly.service;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.dto.QuizDtos.*;
import com.tutorly.model.Quiz;
import com.tutorly.model.QuizAttempt;
import com.tutorly.model.QuizQuestion;
import com.tutorly.model.Student;
import com.tutorly.repository.QuizAttemptRepository;
import com.tutorly.repository.QuizRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizzes;
    private final QuizAttemptRepository attempts;
    private final AuthStudentRepository students;

    public QuizService(QuizRepository quizzes, QuizAttemptRepository attempts,
                       AuthStudentRepository students) {
        this.quizzes = quizzes;
        this.attempts = attempts;
        this.students = students;
    }

    public List<Summary> list(Long studentId) {
        requireStudent(studentId);
        return quizzes.findAllByOrderByIdAsc().stream().map(Summary::from).toList();
    }

    public Detail get(Long studentId, Long quizId) {
        requireStudent(studentId);
        return Detail.from(requireQuiz(quizId));
    }

    @Transactional
    public Attempt submit(Long studentId, Long quizId, Submission request) {
        Student student = requireStudent(studentId);
        Quiz quiz = requireQuiz(quizId);
        List<QuizQuestion> questions = quiz.getQuestions();

        if (questions.isEmpty() || questions.size() > 100) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This quiz is not ready for submissions.");
        }
        if (request.answers() == null || request.answers().size() != questions.size()) {
            throw badRequest("Answer every question exactly once.");
        }

        Set<Long> questionIds = questions.stream()
                .map(QuizQuestion::getId).collect(Collectors.toSet());
        Map<Long, Integer> selected = new HashMap<>();

        for (Answer answer : request.answers()) {
            if (answer == null || answer.questionId() == null
                    || !questionIds.contains(answer.questionId())
                    || answer.selectedOption() == null
                    || answer.selectedOption() < 0 || answer.selectedOption() > 3) {
                throw badRequest("Use valid question IDs and option indexes from 0 to 3.");
            }
            if (selected.putIfAbsent(answer.questionId(), answer.selectedOption()) != null) {
                throw badRequest("Each question may appear only once.");
            }
        }

        int correct = 0;
        for (QuizQuestion question : questions) {
            if (selected.get(question.getId()).intValue() == question.getCorrectOption()) {
                correct++;
            }
        }

        QuizAttempt attempt = new QuizAttempt(student, quiz, correct, questions.size());
        return Attempt.from(attempts.save(attempt));
    }

    public List<Attempt> history(Long studentId) {
        requireStudent(studentId);
        return attempts.findByStudent_IdOrderByCreatedAtDescIdDesc(studentId)
                .stream().map(Attempt::from).toList();
    }

    public Attempt getAttempt(Long studentId, Long attemptId) {
        requireStudent(studentId);
        return attempts.findByIdAndStudent_Id(attemptId, studentId)
                .map(Attempt::from).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found."));
    }

    private Quiz requireQuiz(Long quizId) {
        return quizzes.findWithQuestionsById(quizId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found."));
    }

    private Student requireStudent(Long studentId) {
        return students.findById(studentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Your student account is no longer available. Log in again."));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
