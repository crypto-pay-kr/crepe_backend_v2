package dev.crepe.domain.bank.util;

import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckAlreadyField {

    private final BankRepository bankRepository;
    private final ExceptionDbService exceptionDbService;

    public void validate(BankDataRequest request) {
        if (bankRepository.existsByEmail(request.getBankSignupDataRequest().getEmail())) {

            throw exceptionDbService.getException("BANK_005");
        }

        if (bankRepository.existsByName(request.getBankSignupDataRequest().getName())) {
            throw exceptionDbService.getException("BANK_006");
        }

        if (bankRepository.existsByBankCode(request.getBankSignupDataRequest().getBankPhoneNum())) {
            throw exceptionDbService.getException("BANK_004");
        }
    }
}