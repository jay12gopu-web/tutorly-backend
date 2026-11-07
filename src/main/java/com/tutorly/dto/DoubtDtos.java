package com.tutorly.dto;

import com.tutorly.model.*;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import java.time.Instant;
import java.util.List;

public final class DoubtDtos {
    private DoubtDtos() {}
    public record Create(@NotNull @Positive Long teacherId,
                         @NotBlank @Size(max=120) String title,
                         @NotBlank @Size(max=60) String subject,
                         @NotBlank @Size(max=4000) String question) {}
    public record ReplyInput(@NotBlank @Size(max=4000) String body) {}
    public record Summary(Long id,Long studentId,String studentName,Long teacherId,String teacherName,
                          String title,String subject,TeacherDoubt.Status status,
                          Instant createdAt,Instant updatedAt,Instant resolvedAt) {
        public static Summary from(TeacherDoubt d){return new Summary(d.getId(),d.getStudent().getId(),
            d.getStudent().getName(),d.getTeacher().getId(),d.getTeacher().getName(),d.getTitle(),
            d.getSubject(),d.getStatus(),d.getCreatedAt(),d.getUpdatedAt(),d.getResolvedAt());}
    }
    public record Detail(Summary doubt,String question) {
        public static Detail from(TeacherDoubt d){return new Detail(Summary.from(d),d.getQuestion());}
    }
    public record Reply(Long id,DoubtReply.AuthorRole authorRole,String body,Instant createdAt) {
        public static Reply from(DoubtReply r){return new Reply(r.getId(),r.getAuthorRole(),r.getBody(),r.getCreatedAt());}
    }
    public record Results<T>(List<T> items,int page,int size,long totalElements,int totalPages,boolean hasNext) {
        public static <T> Results<T> from(Page<T> p){return new Results<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.hasNext());}
    }
}
