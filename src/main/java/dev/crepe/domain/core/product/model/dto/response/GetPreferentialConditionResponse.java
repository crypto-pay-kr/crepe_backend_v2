package dev.crepe.domain.core.product.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetPreferentialConditionResponse {
private String title;
private String description;
private float rate;
        }