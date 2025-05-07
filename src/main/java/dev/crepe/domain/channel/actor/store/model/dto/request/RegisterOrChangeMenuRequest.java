package dev.crepe.domain.channel.actor.store.model.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "메뉴 등록/수정 요청 DTO")
public class RegisterOrChangeMenuRequest {

    @Schema(description = "메뉴 이름", example = "크림 크레페")
    private String name;

    @Schema(description = "메뉴 가격", example = "8500")
    private int price;

}
