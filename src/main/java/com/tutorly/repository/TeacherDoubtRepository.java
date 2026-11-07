package com.tutorly.repository;

import com.tutorly.model.TeacherDoubt;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TeacherDoubtRepository extends JpaRepository<TeacherDoubt,Long> {
    Page<TeacherDoubt> findByStudent_Id(Long studentId,Pageable page);
    Page<TeacherDoubt> findByTeacher_Id(Long teacherId,Pageable page);
    Optional<TeacherDoubt> findByIdAndStudent_Id(Long id,Long studentId);
    Optional<TeacherDoubt> findByIdAndTeacher_Id(Long id,Long teacherId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from TeacherDoubt d where d.id=:id and d.student.id=:owner")
    Optional<TeacherDoubt> lockForStudent(@Param("id") Long id,@Param("owner") Long owner);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from TeacherDoubt d where d.id=:id and d.teacher.id=:owner")
    Optional<TeacherDoubt> lockForTeacher(@Param("id") Long id,@Param("owner") Long owner);
}
