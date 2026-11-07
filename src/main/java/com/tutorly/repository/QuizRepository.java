package com.tutorly.repository;

import com.tutorly.model.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "questions")
    Optional<Quiz> findWithQuestionsById(Long id);
}
