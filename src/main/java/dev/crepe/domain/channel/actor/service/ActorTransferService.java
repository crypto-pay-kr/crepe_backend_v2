package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;

import java.math.BigDecimal;

public interface ActorTransferService {
    void requestWithdraw(GetWithdrawRequest request, String email,String traceId);
    void requestTransfer(GetTransferRequest request, String email, String traceId);
    String getAccountHolderName(String ReceiverEmail,String senderEmailString,String currency);
    void requestDeposit(GetDepositRequest request, String email,String traceId);
    String requestTokenDeposit (String userEmail, Long subscribeId, BigDecimal amount , String traceId);
}