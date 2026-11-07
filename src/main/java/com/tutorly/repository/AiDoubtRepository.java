package com.tutorly.repository;
import com.tutorly.model.AiDoubt;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AiDoubtRepository extends JpaRepository<AiDoubt,Long>{
    Optional<AiDoubt> findByStudent_IdAndRequestId(Long studentId,String requestId);
    Optional<AiDoubt> findByIdAndStudent_Id(Long id,Long studentId);
    Page<AiDoubt> findByStudent_Id(Long studentId,Pageable page);
    List<AiDoubt> findByStudent_IdAndStatus(Long studentId,AiDoubt.Status status);
    long countByStudent_IdAndRequestDay(Long studentId,LocalDate day);
}
