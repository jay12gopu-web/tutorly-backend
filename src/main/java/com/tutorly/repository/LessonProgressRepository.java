package com.tutorly.repository;

import com.tutorly.model.LessonProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByStudent_IdAndLesson_Id(Long studentId, Long lessonId);

    @EntityGraph(attributePaths = {"lesson", "lesson.steps"})
    List<LessonProgress> findByStudent_IdOrderByUpdatedAtDescIdDesc(Long studentId);
}
