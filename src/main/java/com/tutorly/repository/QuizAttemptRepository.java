package com.tutorly.repository;

import com.tutorly.model.QuizAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @EntityGraph(attributePaths = "quiz")
    List<QuizAttempt> findByStudent_IdOrderByCreatedAtDescIdDesc(Long studentId);

    @EntityGraph(attributePaths = "quiz")
    Optional<QuizAttempt> findByIdAndStudent_Id(Long id, Long studentId);
}
