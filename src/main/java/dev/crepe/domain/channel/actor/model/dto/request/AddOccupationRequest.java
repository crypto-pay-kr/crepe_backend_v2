package dev.crepe.domain.channel.actor.model.dto.request;

import dev.crepe.domain.core.product.model.dto.eligibility.Occupation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "직업 추가, 휴대폰 인증 DTO")
public class AddOccupationRequest {
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    @Schema(description = "전화번호 (010으로 시작하는 11자리)", example = "01098765432")
    private String phoneNumber;
    private Occupation occupation;

}
