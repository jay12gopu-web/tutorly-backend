package com.tutorly.repository;
import com.tutorly.model.LiveSessionMeeting;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LiveSessionMeetingRepository extends JpaRepository<LiveSessionMeeting,Long> {
    // Scalar ownership lookup avoids loading a stale booking before its teacher lock.
    @Query("select b.slot.teacher.id from SessionBooking b where b.id=:bookingId and b.student.id=:studentId")
    Optional<Long> teacherForStudentBooking(@Param("bookingId") Long bookingId,@Param("studentId") Long studentId);
}
