package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorWithdrawService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorWithdrawServiceImpl implements ActorWithdrawService {

    private final WithdrawService withdrawService;

    @Override
    public void requestWithdraw(GetWithdrawRequest request, String email) {
        withdrawService.requestWithdraw(request, email);
    }
}