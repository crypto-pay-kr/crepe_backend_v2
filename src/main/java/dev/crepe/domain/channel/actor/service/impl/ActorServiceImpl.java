package dev.crepe.domain.channel.actor.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.AuthenticationToken;
import dev.crepe.domain.auth.jwt.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.auth.model.JwtToken;
import dev.crepe.domain.channel.actor.exception.*;
import dev.crepe.domain.channel.actor.model.dto.request.*;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.store.exception.StoreNotFoundException;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.global.model.dto.ApiResponse;
import dev.crepe.infra.sms.model.InMemorySmsAuthService;
import dev.crepe.infra.sms.model.SmsType;
import dev.crepe.infra.sms.service.SmsManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ActorServiceImpl  implements ActorService {

    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;
    private final SmsManageService smsManageService;


    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {

        Actor actor = actorRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new); // Optional에서 Actor 추출

        if (!encoder.matches(request.getPassword(), actor.getPassword())) {
            throw new LoginFailedException();
        }

        AuthenticationToken token;
        token = jwtTokenProvider.createToken(actor.getEmail(),actor.getRole());



        tokenRepository.findById(actor.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateTokens(token.getAccessToken(), token.getRefreshToken()),
                        () -> tokenRepository.save(new JwtToken(actor.getId(),actor.getRole(), token.getAccessToken(), token.getRefreshToken()))
                );

        TokenResponse tokenResponse = new TokenResponse(token, actor);
        return ApiResponse.success(actor.getRole() + " 로그인 성공", tokenResponse);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> changePassword(ChangePasswordRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        if(!encoder.matches(request.getOldPassword(), actor.getPassword())) {
            throw new InvalidPasswordException();
        }

        if(request.getOldPassword().equals(request.getNewPassword())) {
            throw new CannotSamePasswordException();
        }

        actor.changePassword(encoder.encode(request.getNewPassword()));
        return ResponseEntity.ok(null);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> changePhone(ChangePhoneRequest request, String userEmail) {

        Actor store = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        InMemorySmsAuthService.SmsAuthData smsAuthData = smsManageService.getSmsAuthData(request.getPhoneNumber(), SmsType.SIGN_UP);
        String successNewPhone = smsAuthData.getPhoneNumber();

        store.changePhone(successNewPhone);
        return ResponseEntity.ok(null);
    }


    @Override
    @Transactional
    public ResponseEntity<Void> changeName(ChangeNameRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new StoreNotFoundException(userEmail));

        actor.changeName(request.getNewName());
        return ResponseEntity.ok(null);
    }


}
