package dev.crepe.domain.bank.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "은행 가입 정보 DTO")
public class BankDataRequest {

    @Schema(description = "은행 회원가입 데이터")
    private BankSignupDataRequest bankSignupDataRequest;

    @Schema(description = "은행 CI 이미지 파일")
    private MultipartFile bankCiImage;
}