package dev.crepe.domain.bank.util;

import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.repository.BankRepository;
import dev.crepe.domain.channel.actor.exception.AlreadyEmailException;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
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
            throw exceptionDbService.getException("ACTOR_003"); // 이미 존재하는 이메일
        }

        if (bankRepository.existsByName(request.getBankSignupDataRequest().getName())) {
            throw exceptionDbService.getException("ACTOR_004"); // 이미 존재하는 닉네임
        }

        if (bankRepository.existsByBankCode(request.getBankSignupDataRequest().getBankPhoneNum())) {
            throw exceptionDbService.getException("ACTOR_009"); // 이미 존재하는 휴대폰 번호
        }
    }
}