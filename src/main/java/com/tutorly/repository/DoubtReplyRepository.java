package com.tutorly.repository;
import com.tutorly.model.DoubtReply;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DoubtReplyRepository extends JpaRepository<DoubtReply,Long> {
    Page<DoubtReply> findByDoubt_Id(Long doubtId,Pageable page);
}
