package dev.crepe.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferentialConditionDto {
    private Long id;
    private String title;
    private Float rate;
    private String description;
}
