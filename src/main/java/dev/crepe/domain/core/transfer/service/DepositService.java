package dev.crepe.domain.core.transfer.service;
import dev.crepe.domain.core.transfer.model.dto.requset.GetDepositRequest;
import org.springframework.stereotype.Service;


@Service
public interface DepositService {

    void requestDeposit(GetDepositRequest request, String email);
}
