package com.tutorly.dto;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public final class NearbyTeacherDtos {
    private NearbyTeacherDtos(){}
    public record LocationInput(@NotNull Double latitude,@NotNull Double longitude,@NotNull Boolean enabled){}
    public record OwnLocation(Long teacherId,boolean configured,BigDecimal latitude,BigDecimal longitude,boolean enabled,boolean discoverable,Instant updatedAt){}
    public record Match(TeacherDtos.PublicProfile teacher,BigDecimal distanceKm){}
    public record Results(List<Match> items,int page,int size,long totalElements,int totalPages,boolean hasNext,double radiusKm,String distanceType){}
}
