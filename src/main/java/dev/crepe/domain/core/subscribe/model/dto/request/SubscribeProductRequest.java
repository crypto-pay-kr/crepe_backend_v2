package dev.crepe.domain.core.subscribe.model.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscribeProductRequest {
    private Long productId;
}