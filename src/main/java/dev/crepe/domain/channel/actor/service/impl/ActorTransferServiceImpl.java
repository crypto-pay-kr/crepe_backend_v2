package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.channel.actor.service.ActorTransferService;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.domain.core.deposit.service.TokenDepositService;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetTransferRequest;
import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;
import dev.crepe.domain.core.transfer.service.DepositService;
import dev.crepe.domain.core.transfer.service.TransferService;
import dev.crepe.domain.core.transfer.service.WithdrawService;
import dev.crepe.global.util.RedisDeduplicationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ActorTransferServiceImpl implements ActorTransferService {

    private final WithdrawService withdrawService;
    private final TransferService transferService;
    private final AccountService accountService;
    private final DepositService depositService;
    private final RedisDeduplicationUtil redisDeduplicationUtil;
    private final TokenDepositService tokenDepositService;
    @Override
    public void requestWithdraw(GetWithdrawRequest request, String email,String traceId) {
        String redisKey = "dedup:withdraw:" + email + ":" + traceId;
        redisDeduplicationUtil.checkAndStoreIfDuplicate(redisKey);
        withdrawService.requestWithdraw(request, email);
    }

    @Override
    @Transactional
    public void requestTransfer(GetTransferRequest request, String email,String traceId) {
        String redisKey = "dedup:transfer:" + email + ":" + traceId;
        redisDeduplicationUtil.checkAndStoreIfDuplicate(redisKey);
        transferService.requestTransfer(request, email);
    }

    @Override
    public String getAccountHolderName(String receiverEmail,String senderEmail,String currency) {
      return accountService.getAccountHolderName(receiverEmail,senderEmail, currency);
    }

    @Override
    public void requestDeposit(GetDepositRequest request, String email,String traceId) {
        String redisKey = "dedup:withdraw:" + email + ":" +request.getTxid()+":"+ traceId;
        redisDeduplicationUtil.checkAndStoreIfDuplicate(redisKey);
        depositService.requestDeposit(request, email);
    }

    @Override
    public String requestTokenDeposit (String userEmail, Long subscribeId, BigDecimal amount ,String traceId)  {
        String redisKey = "dedup:token-deposit:" + userEmail + ":" + traceId;
        redisDeduplicationUtil.checkAndStoreIfDuplicate(redisKey);
        String result =tokenDepositService.depositToProduct(userEmail,subscribeId, amount);
        return result;
    }

}