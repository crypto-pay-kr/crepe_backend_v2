package dev.crepe.domain.admin.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetActorInfoResponse {

    private Long actorId;
    private String actorName;
    private String actorEmail;
    private String actorPhoneNum;
    private String actorStatus;
    private String actorRole;
}
