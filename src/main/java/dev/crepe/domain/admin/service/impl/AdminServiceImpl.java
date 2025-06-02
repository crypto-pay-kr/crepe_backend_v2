package dev.crepe.domain.admin.service.impl;

import dev.crepe.domain.admin.service.AdminService;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.sse.service.impl.AuthServiceImpl;
import dev.crepe.domain.channel.actor.exception.LoginFailedException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final AuthServiceImpl authService;
    private final ExceptionDbService exceptionDbService;
    // 관리자 로그인
    @Override
    @Transactional
    public ApiResponse<TokenResponse> adminLogin(LoginRequest request) {

        Actor actor = actorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> exceptionDbService.getException("ACTOR_002"));

        if (!encoder.matches(request.getPassword(), actor.getPassword())) {
            throw exceptionDbService.getException("ACTOR_013");
        }

        // 관리자 권한 확인 (선택사항)
        if (!actor.getRole().isAdmin()) {
            throw exceptionDbService.getException("ACTOR_001");
        }

        // AuthService를 통해 토큰 생성 및 저장 (중복 로그인 방지 + 실시간 알림)
        AuthenticationToken token = authService.createAndSaveToken(actor.getEmail(), actor.getRole());

        TokenResponse tokenResponse = new TokenResponse(token, actor);
        return ApiResponse.success(actor.getRole() + " 로그인 성공", tokenResponse);
    }


}
