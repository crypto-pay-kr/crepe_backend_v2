package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;

public interface ActorWithdrawService {
    void requestWithdraw(GetWithdrawRequest request, String email);
}