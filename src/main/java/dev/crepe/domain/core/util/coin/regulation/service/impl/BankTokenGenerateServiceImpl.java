package dev.crepe.domain.core.util.coin.regulation.service.impl;

import dev.crepe.domain.core.util.coin.regulation.model.dto.request.GenerateTokenRequest;
import dev.crepe.domain.core.util.coin.regulation.service.BankTokenGenerateService;
import org.springframework.http.ResponseEntity;

public class BankTokenGenerateServiceImpl implements BankTokenGenerateService {

    @Override
    public ResponseEntity<Void> requestTokenGenerate(GenerateTokenRequest request, String bankEmail) {

        // TODO : 은행 사용자 권한 검증


        // TODO : 발행 요청 정보 유효성 검사(최소/최대 발행량, 일일 한도 등)

        // TODO : 포토폴리오 포함 계좌 등록 확인


        // TODO : 관리자 발행 요청 전송

        return ResponseEntity.ok(null);

    }
}
