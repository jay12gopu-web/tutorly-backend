package com.tutorly.repository;

import com.tutorly.model.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "steps")
    Optional<Lesson> findWithStepsById(Long id);
}
