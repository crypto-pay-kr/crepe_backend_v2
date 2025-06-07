package dev.crepe.domain.core.transfer.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Builder
@Schema(description = "입금 확인 응답 DTO")
public class GetDepositResponse {

    private String state;
    private String amount;
    private String currency;


}
