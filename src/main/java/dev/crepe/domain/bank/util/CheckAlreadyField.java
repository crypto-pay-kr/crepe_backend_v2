package dev.crepe.domain.bank.util;

import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.channel.actor.exception.AlreadyEmailException;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckAlreadyField {

    private final BankRepository bankRepository;

    public void validate(BankDataRequest request) {
        if (bankRepository.existsByEmail(request.getBankSignupDataRequest().getEmail())) {
            throw new AlreadyEmailException();
        }

        if (bankRepository.existsByName(request.getBankSignupDataRequest().getName())) {
            throw new AlreadyNicknameException();
        }

        if (bankRepository.existsByBankCode(request.getBankSignupDataRequest().getBankPhoneNum())) {
            throw new AlreadyPhoneNumberException();
        }
    }
}