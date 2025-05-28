package dev.crepe.domain.bank.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetBankDashboardResponse {
    private long bankCount;
    private long productCount;
    private long bankTokenCount;
}
