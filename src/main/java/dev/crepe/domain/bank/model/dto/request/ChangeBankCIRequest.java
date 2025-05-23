package dev.crepe.domain.bank.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "CI 이미지 변경 DTO")
public class ChangeBankCIRequest {

    @NotNull(message = "CI 이미지는 필수 입력값입니다.")
    private MultipartFile ciImage;
}