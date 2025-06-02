package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;

public interface ActorTransferService {
    void requestWithdraw(GetWithdrawRequest request, String email);
    void requestTransfer(GetTransferRequest request, String email);
    String getAccountHolderName(String ReceiverEmail,String senderEmailString,String currency);
    void requestDeposit(GetDepositRequest request, String email);
}