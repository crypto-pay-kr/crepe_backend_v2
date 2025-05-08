package dev.crepe.domain.core.transfer.service;


import dev.crepe.domain.core.transfer.model.dto.requset.GetWithdrawRequest;

public interface WithdrawService {

    void requestWithdraw(GetWithdrawRequest request,String email);
}
