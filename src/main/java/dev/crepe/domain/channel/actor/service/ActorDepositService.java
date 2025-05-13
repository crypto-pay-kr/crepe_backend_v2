package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;

public interface ActorDepositService {

    void requestDeposit(GetDepositRequest request, String email);
}
