package com.tutorly.repository;

import com.tutorly.model.SessionBooking;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SessionBookingRepository extends JpaRepository<SessionBooking,Long> {
    @Query("select b.slot.teacher.id from SessionBooking b where b.id=:id")
    Optional<Long> teacherId(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from SessionBooking b where b.id=:id")
    Optional<SessionBooking> lockById(@Param("id") Long id);
    Optional<SessionBooking> findBySlot_IdAndStatus(Long slotId,SessionBooking.Status status);
    boolean existsBySlot_IdAndStatus(Long slotId,SessionBooking.Status status);
    @Query("""
        select count(b) from SessionBooking b where b.student.id=:studentId and b.status=:status
        and b.slot.startsAt < :endsAt and b.slot.endsAt > :startsAt
        """)
    long countStudentOverlaps(@Param("studentId") Long studentId,@Param("startsAt") Instant startsAt,
                               @Param("endsAt") Instant endsAt,@Param("status") SessionBooking.Status status);
    Page<SessionBooking> findByStudent_Id(Long studentId,Pageable pageable);
    Page<SessionBooking> findBySlot_Teacher_Id(Long teacherId,Pageable pageable);
}
