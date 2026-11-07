package com.tutorly.dto;
import jakarta.validation.constraints.*;
import java.time.Instant;

public final class LiveSessionDtos {
    private LiveSessionDtos() {}
    public record MeetingInput(@NotBlank @Size(max=2048) String meetingUrl) {}
    public record Meeting(Long slotId,boolean configured,String meetingUrl,Instant updatedAt) {}
    public enum JoinState { READY, TOO_EARLY, ENDED, NOT_CONFIGURED, CANCELLED, IN_PERSON }
    public record JoinAccess(Long bookingId,Long slotId,JoinState state,
                             Instant joinOpensAt,Instant joinClosesAt,String meetingUrl) {}
}
