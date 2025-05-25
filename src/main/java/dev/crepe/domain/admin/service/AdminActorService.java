package dev.crepe.domain.admin.service;

import dev.crepe.domain.admin.dto.request.ChangeActorStatusRequest;
import dev.crepe.domain.admin.dto.response.ChangeActorStatusResponse;
import dev.crepe.domain.admin.dto.response.GetAccountInfoResponse;
import dev.crepe.domain.admin.dto.response.GetActorInfoResponse;
import dev.crepe.domain.channel.actor.model.ActorStatus;
import org.springframework.data.domain.Page;


public interface AdminActorService {

    Page<GetActorInfoResponse> getActorsByRole(String role,  ActorStatus status, int page, int size);

    ChangeActorStatusResponse changeActorStatus(ChangeActorStatusRequest request);
}
