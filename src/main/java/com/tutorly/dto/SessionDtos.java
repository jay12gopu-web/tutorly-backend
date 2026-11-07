package com.tutorly.dto;

import com.tutorly.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;

public final class SessionDtos {
    private SessionDtos() {}
    public record CreateSlot(@NotBlank @Size(max=120) String title,
                             @NotBlank @Size(max=60) String subject,
                             @NotNull @Future Instant startsAt,
                             @NotNull @Min(15) @Max(180) Integer durationMinutes,
                             @NotNull TutorSessionSlot.SessionType sessionType,
                             @NotNull TutorSessionSlot.DeliveryMode deliveryMode) {}
    public record Book(@NotNull @Positive Long slotId) {}
    public record Slot(Long id,Long teacherId,String teacherName,String title,String subject,
                       Instant startsAt,Instant endsAt,TutorSessionSlot.SessionType sessionType,
                       TutorSessionSlot.DeliveryMode deliveryMode,boolean cancelled,boolean booked) {
        public static Slot from(TutorSessionSlot s,boolean booked) {
            return new Slot(s.getId(),s.getTeacher().getId(),s.getTeacher().getName(),s.getTitle(),s.getSubject(),
                    s.getStartsAt(),s.getEndsAt(),s.getSessionType(),s.getDeliveryMode(),s.isCancelled(),booked);
        }
    }
    public record Booking(Long id,Long slotId,Long teacherId,String teacherName,Long studentId,String studentName,
                          String title,String subject,Instant startsAt,Instant endsAt,
                          TutorSessionSlot.SessionType sessionType,TutorSessionSlot.DeliveryMode deliveryMode,
                          SessionBooking.Status status,Instant createdAt,Instant cancelledAt) {
        public static Booking from(SessionBooking b) {
            var s=b.getSlot();return new Booking(b.getId(),s.getId(),s.getTeacher().getId(),s.getTeacher().getName(),
                    b.getStudent().getId(),b.getStudent().getName(),s.getTitle(),s.getSubject(),s.getStartsAt(),s.getEndsAt(),
                    s.getSessionType(),s.getDeliveryMode(),b.getStatus(),b.getCreatedAt(),b.getCancelledAt());
        }
    }
    public record Results<T>(List<T> items,int page,int size,long totalElements,int totalPages,boolean hasNext) {
        public static <T> Results<T> from(Page<T> p) {
            return new Results<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.hasNext());
        }
    }
}
