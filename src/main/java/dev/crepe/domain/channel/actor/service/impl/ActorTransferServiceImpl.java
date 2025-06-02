package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorTransferService;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.DepositService;
import dev.crepe.domain.core.transfer.service.TransferService;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorTransferServiceImpl implements ActorTransferService {

    private final WithdrawService withdrawService;
    private final TransferService transferService;
    private final AccountService accountService;
    private final DepositService depositService;

    @Override
    public void requestWithdraw(GetWithdrawRequest request, String email) {
        withdrawService.requestWithdraw(request, email);
    }

    @Override
    public void requestTransfer(GetTransferRequest request, String email) {
        transferService.requestTransfer(request, email);
    }

    @Override
    public String getAccountHolderName(String receiverEmail,String senderEmail,String currency) {
      return accountService.getAccountHolderName(receiverEmail,senderEmail, currency);
    }



    @Override
    public void requestDeposit(GetDepositRequest request, String email) {
        depositService.requestDeposit(request, email);
    }

}