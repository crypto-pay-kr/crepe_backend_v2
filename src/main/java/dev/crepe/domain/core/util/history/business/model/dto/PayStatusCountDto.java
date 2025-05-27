package dev.crepe.domain.core.util.history.business.model.dto;

import dev.crepe.domain.core.util.history.business.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class PayStatusCountDto {
    private TransactionStatus status;
    private Long count;
}
