package dev.crepe.domain.channel.actor.store.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "가맹점 사업자 등록번호 수정 요청 DTO")
public class ChangeBusinessInfoRequest {
    private String businessNumber;
}

