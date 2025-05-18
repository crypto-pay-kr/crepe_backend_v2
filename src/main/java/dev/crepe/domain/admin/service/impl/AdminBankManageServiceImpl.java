package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.dto.request.RejectBankTokenRequest;
import dev.crepe.domain.admin.dto.response.GetAllBankTokenResponse;
import dev.crepe.domain.admin.service.AdminBankManageService;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.core.util.coin.regulation.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBankManageServiceImpl implements AdminBankManageService {


    private final TokenService tokenService;
    private final BankService bankService;


    // 은행 계정 생성
    @Override
    @Transactional
    public void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage) {

        BankDataRequest bankDataRequest = new BankDataRequest(request, bankCiImage);
        bankService.signup(bankDataRequest);
    }

    // 전체 은행토큰 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetAllBankTokenResponse> getAllBankTokenResponseList(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return tokenService.getAllBankTokenResponseList(pageRequest);

    }

    // 은행 토큰 발행 요청 승인
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveBankTokenRequest(Long tokenHistoryId) {
        tokenService.approveBankTokenRequest(tokenHistoryId);
    }


    // 은행 토큰 발행 요청 반려
    @Transactional
    @Override
    public void rejectBankTokenRequest(RejectBankTokenRequest request, Long tokenHistoryId) {
        tokenService.rejectBankTokenRequest(request, tokenHistoryId);
    }
}
