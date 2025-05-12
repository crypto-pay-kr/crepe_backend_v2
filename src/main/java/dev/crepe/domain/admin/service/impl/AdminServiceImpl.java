package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.AuthenticationToken;
import dev.crepe.domain.auth.jwt.JwtTokenProvider;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import dev.crepe.domain.auth.jwt.repository.TokenRepository;
import dev.crepe.domain.bank.model.dto.request.BankDataRequest;
import dev.crepe.domain.bank.model.dto.request.BankSignupDataRequest;
import dev.crepe.domain.bank.service.BankService;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BankService bankService;
    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request) {

        Actor actor = actorRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailedException::new);

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
    public void bankSignup(BankSignupDataRequest request, MultipartFile bankCiImage) {

        BankDataRequest bankDataRequest = new BankDataRequest(request, bankCiImage);
        bankService.signup(bankDataRequest);
    }




}
