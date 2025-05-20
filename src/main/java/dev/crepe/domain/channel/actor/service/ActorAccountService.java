package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;

import java.util.List;

public interface ActorAccountService {

    // 출금 주소 최초 등록
    void createAccountAddress(GetAddressRequest request, String email);

    // 출금 주소 재등록
    void reRegisterAddress(GetAddressRequest request, String email);

    // 특정 코인 출금 주소 조회
    GetAddressResponse getAddressByCurrency(String currency, String email);

    // 전체 잔액 조회
    List<GetBalanceResponse> getBalanceList(String email);

    // 특정 코인 잔액 조회
    GetBalanceResponse getBalanceByCurrency(String email, String currency);

    //계좌 등록 해제
    void unRegisterAccount(String email, String currency);

}