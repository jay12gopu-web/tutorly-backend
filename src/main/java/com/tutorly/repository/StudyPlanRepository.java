package com.tutorly.repository;

import com.tutorly.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByStudent_IdOrderByStartsAtAscIdAsc(Long studentId);

    Optional<StudyPlan> findByIdAndStudent_Id(Long id, Long studentId);
}
