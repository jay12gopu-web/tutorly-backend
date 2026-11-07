package com.tutorly.dto;
import com.tutorly.model.AiDoubt;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;
public final class AiDoubtDtos {
    private AiDoubtDtos(){}
    public record Ask(@NotNull UUID requestId,@NotBlank @Size(max=60) String subject,@NotBlank @Size(max=4000) String question){}
    public record Detail(Long id,String requestId,String subject,String question,AiDoubt.Status status,String answer,
                         String errorCode,boolean truncated,String model,Instant createdAt,Instant completedAt){
        public static Detail from(AiDoubt d){return new Detail(d.getId(),d.getRequestId(),d.getSubject(),d.getQuestion(),d.getStatus(),d.getAnswer(),d.getErrorCode(),d.isTruncated(),d.getModel(),d.getCreatedAt(),d.getCompletedAt());}
    }
    public record Usage(boolean enabled,boolean configured,String provider,String model,int dailyLimit,long attemptsUsed,long attemptsRemaining,Instant resetsAt){}
    public record Results<T>(List<T> items,int page,int size,long totalElements,int totalPages,boolean hasNext){
        public static <T> Results<T> from(Page<T> p){return new Results<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages(),p.hasNext());}
    }
}
