package dev.crepe.domain.bank.util;

import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.channel.actor.exception.AlreadyEmailException;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckAlreadyField {

    private final ActorRepository actorRepository;

    public void validate(BankDataRequest request) {
        if (actorRepository.existsByEmail(request.getBankSignupDataRequest().getEmail())) {
            throw new AlreadyEmailException();
        }

        if (actorRepository.existsByName(request.getBankSignupDataRequest().getName())) {
            throw new AlreadyNicknameException();
        }

        if (actorRepository.existsByPhoneNum(request.getBankSignupDataRequest().getBankPhoneNum())) {
            throw new AlreadyPhoneNumberException();
        }
    }
}