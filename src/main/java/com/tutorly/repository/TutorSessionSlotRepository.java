package com.tutorly.repository;

import com.tutorly.model.*;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TutorSessionSlotRepository extends JpaRepository<TutorSessionSlot,Long> {
    @Query("select s.teacher.id from TutorSessionSlot s where s.id=:id")
    Optional<Long> teacherId(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TutorSessionSlot s where s.id=:id")
    Optional<TutorSessionSlot> lockById(@Param("id") Long id);
    @Query("""
        select count(s) from TutorSessionSlot s where s.teacher.id=:teacherId and s.cancelled=false
        and s.startsAt < :endsAt and s.endsAt > :startsAt
        """)
    long countOverlaps(@Param("teacherId") Long teacherId,@Param("startsAt") Instant startsAt,@Param("endsAt") Instant endsAt);
    Page<TutorSessionSlot> findByTeacher_Id(Long teacherId,Pageable pageable);
    @Query(value="""
        select s from TutorSessionSlot s where s.teacher.id=:teacherId and s.teacher.listed=true
        and s.cancelled=false and s.startsAt > :now
        and not exists (select b.id from SessionBooking b where b.slot=s and b.status=:status)
        """,countQuery="""
        select count(s) from TutorSessionSlot s where s.teacher.id=:teacherId and s.teacher.listed=true
        and s.cancelled=false and s.startsAt > :now
        and not exists (select b.id from SessionBooking b where b.slot=s and b.status=:status)
        """)
    Page<TutorSessionSlot> available(@Param("teacherId") Long teacherId,@Param("now") Instant now,
                                     @Param("status") SessionBooking.Status status,Pageable pageable);
}
