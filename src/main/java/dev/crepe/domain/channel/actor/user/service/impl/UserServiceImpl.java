package dev.crepe.domain.channel.actor.user.service.impl;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.channel.actor.user.model.dto.ChangeNicknameRequest;
import dev.crepe.domain.channel.actor.user.model.dto.UserInfoResponse;
import dev.crepe.domain.channel.actor.user.model.dto.UserSignupRequest;
import dev.crepe.domain.channel.actor.user.repository.UserRepository;
import dev.crepe.domain.channel.actor.user.service.UserService;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ActorRepository actorRepository;
    private final PasswordEncoder encoder;
    private final AccountService accountService;
    private final ExceptionDbService exceptionDbService;

    // 회원가입
    @Override
    @Transactional
    public ApiResponse<ResponseEntity<Void>> signup(UserSignupRequest request) {

        // sms 인증 비활성화 -> 사용시 주석 해제
//        InMemorySmsAuthService.SmsAuthData smsAuthData = smsManageService.getSmsAuthData(request.getPhoneNumber(), SmsType.SIGN_UP);
//        String validatePhone = smsAuthData.getPhoneNumber();

        checkAlreadyField(request);

        Actor user = Actor.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .nickName(request.getNickname())
//                .phoneNum(validatePhone)
                .phoneNum(request.getPhoneNumber())
                .name(request.getName())
                .role(UserRole.USER)
                .build();

        actorRepository.save(user);

        return ApiResponse.success("회원가입 성공", null);
    }



    private void checkAlreadyField(UserSignupRequest request) {
        if (actorRepository.existsByEmail(request.getEmail())) {
            throw exceptionDbService.getException("ACTOR_003");
        }

        if (actorRepository.existsByNickName(request.getNickname())) {
            throw new AlreadyNicknameException();
        }

        if (actorRepository.existsByPhoneNum(request.getPhoneNumber())) {
            throw new AlreadyPhoneNumberException();
        }
    }

    @Override
    @Transactional
    public void changeNickname(ChangeNicknameRequest request, String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        if(userRepository.existsByNickName(request.getNewNickname())) throw  new AlreadyNicknameException();

        actor.changeNickname(request.getNewNickname());
    }


    // 회원 정보 조회
    @Override
    public UserInfoResponse getUserInfo(String userEmail) {

        Actor actor = actorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        return UserInfoResponse.builder()
                .email(actor.getEmail())
                .name(actor.getName())
                .nickname(actor.getNickName())
                .phoneNumber(actor.getPhoneNum())
                .role(actor.getRole().name())
                .build();
    }


}
