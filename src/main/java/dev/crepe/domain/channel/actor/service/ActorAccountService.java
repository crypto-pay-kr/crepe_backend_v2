package dev.crepe.domain.channel.actor.service;

import dev.crepe.domain.channel.actor.model.dto.response.BankTokenAccountDto;
import dev.crepe.domain.channel.actor.model.dto.response.GetAllBalanceResponse;
import dev.crepe.domain.core.account.model.dto.request.GetAddressRequest;
import dev.crepe.domain.core.account.model.dto.response.GetAddressResponse;
import dev.crepe.domain.core.account.model.dto.response.GetBalanceResponse;
import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.util.coin.model.GetCoinInfo;

import java.math.BigDecimal;
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


    // 내가 가진 계좌, 상품 조회
    List<BankTokenAccountDto> getMyAccountsSubscription(String email);

    // 특정 토큰 잔액 조회 
    BigDecimal getTokenBalance(String email, String currency);


    //계좌 등록 해제
    void unRegisterAccount(String email, String currency);


    // 계좌 정지
    void holdActorAccount(Account account);


    //토큰및 코인 계좌 조회
    GetAllBalanceResponse getAllBalance(String email);

    //코인 정보 조회
    GetCoinInfo getCoinInfo(String currency);
}