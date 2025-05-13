package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorDepositService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorDepositServiceImpl implements ActorDepositService {

    private final DepositService depositService;

    @Override
    public void requestDeposit(GetDepositRequest request, String email) {
        depositService.requestDeposit(request, email);
    }
}