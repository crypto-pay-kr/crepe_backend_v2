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
@Schema(description = "직업 추가 DTO")
public class AddOccupationRequest {

    private Occupation occupation;

}
