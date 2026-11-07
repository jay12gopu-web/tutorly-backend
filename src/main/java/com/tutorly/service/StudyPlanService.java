package com.tutorly.service;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.dto.StudyPlanRequest;
import com.tutorly.dto.StudyPlanResponse;
import com.tutorly.model.Student;
import com.tutorly.model.StudyPlan;
import com.tutorly.repository.StudyPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudyPlanService {

    private final StudyPlanRepository plans;
    private final AuthStudentRepository students;

    public StudyPlanService(StudyPlanRepository plans,
                            AuthStudentRepository students) {
        this.plans = plans;
        this.students = students;
    }

    public List<StudyPlanResponse> getAll(Long studentId) {
        requireStudent(studentId);
        return plans.findByStudent_IdOrderByStartsAtAscIdAsc(studentId)
                .stream().map(StudyPlanResponse::from).toList();
    }

    public StudyPlanResponse getOne(Long studentId, Long planId) {
        requireStudent(studentId);
        return StudyPlanResponse.from(requireOwnedPlan(studentId, planId));
    }

    @Transactional
    public StudyPlanResponse create(Long studentId, StudyPlanRequest request) {
        Student student = requireStudent(studentId);
        StudyPlan plan = new StudyPlan(
                student, request.title().strip(), request.subject().strip(),
                request.startsAt(), request.durationMinutes()
        );
        return StudyPlanResponse.from(plans.save(plan));
    }

    @Transactional
    public StudyPlanResponse setDone(Long studentId, Long planId, boolean done) {
        requireStudent(studentId);
        StudyPlan plan = requireOwnedPlan(studentId, planId);
        plan.setDone(done);
        return StudyPlanResponse.from(plans.save(plan));
    }

    @Transactional
    public void delete(Long studentId, Long planId) {
        requireStudent(studentId);
        plans.delete(requireOwnedPlan(studentId, planId));
    }

    private Student requireStudent(Long studentId) {
        return students.findById(studentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Your student account is no longer available. Log in again.")
        );
    }

    private StudyPlan requireOwnedPlan(Long studentId, Long planId) {
        return plans.findByIdAndStudent_Id(planId, studentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Study plan not found.")
        );
    }
}
