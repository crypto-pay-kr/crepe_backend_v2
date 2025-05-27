package dev.crepe.domain.admin.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetActorInfoResponse {
    private Long actorId;
    private String actorName;
    private String actorEmail;
    private String actorPhoneNum;
    private String actorRole;
    private String actorStatus;
    private SuspensionInfo suspensionInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuspensionInfo {
        private String type;
        private String suspendedAt;
        private String suspendedUntil;
        private String reason;
        private String suspensionPeriod;
    }
}
