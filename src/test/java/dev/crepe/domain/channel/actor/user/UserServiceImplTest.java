package dev.crepe.domain.channel.actor.user;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.util.AuthenticationToken;
import dev.crepe.domain.auth.sse.service.AuthService;
import dev.crepe.domain.channel.actor.exception.AlreadyEmailException;
import dev.crepe.domain.channel.actor.exception.AlreadyNicknameException;
import dev.crepe.domain.channel.actor.exception.AlreadyPhoneNumberException;
import dev.crepe.domain.channel.actor.model.dto.request.LoginRequest;
import dev.crepe.domain.channel.actor.model.dto.response.TokenResponse;
import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import dev.crepe.domain.channel.actor.service.ActorService;
import dev.crepe.domain.channel.actor.user.exception.UserNotFoundException;
import dev.crepe.domain.channel.actor.user.model.dto.ChangeNicknameRequest;
import dev.crepe.domain.channel.actor.user.model.dto.UserInfoResponse;
import dev.crepe.domain.channel.actor.user.model.dto.UserSignupRequest;
import dev.crepe.domain.channel.actor.user.repository.UserRepository;
import dev.crepe.domain.channel.actor.user.service.impl.UserServiceImpl;
import dev.crepe.domain.core.account.service.AccountService;
import dev.crepe.global.error.exception.CustomException;
import dev.crepe.global.error.exception.ExceptionDbService;
import dev.crepe.global.model.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActorRepository actorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private ExceptionDbService exceptionDbService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signup_Success() {
        // given
        UserSignupRequest request = new UserSignupRequest(
                "test@example.com",
                "Password123!",
                "01012345678",
                "Test User",
                "testUser"
        );

        when(actorRepository.existsByEmail(anyString())).thenReturn(false);
        when(actorRepository.existsByNickName(anyString())).thenReturn(false);
        when(actorRepository.existsByPhoneNum(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(actorRepository.save(any(Actor.class))).thenReturn(new Actor());

        // when
        ApiResponse<ResponseEntity<Void>> response = userService.signup(request);

        // then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("회원가입 성공", response.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시도 시 예외 발생")
    void signup_WithExistingEmail() {
        // given
        UserSignupRequest request = new UserSignupRequest(
                "existing@example.com",
                "Password123!",
                "01012345678",
                "Test User",
                "testUser"
        );

        when(actorRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(exceptionDbService.getException("ACTOR_003"))
                .thenThrow(new CustomException("ACTOR_003", null, null));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.signup(request));
        assertEquals("ACTOR_003", exception.getCode());
        verify(actorRepository, never()).save(any(Actor.class));
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 회원가입 시도 시 예외 발생")
    void signup_WithExistingNickName() {
        // given
        UserSignupRequest request = new UserSignupRequest(
                "test@example.com",
                "Password123!",
                "01012345678",
                "Existing User",
                "testUser"
        );

        when(actorRepository.existsByEmail(anyString())).thenReturn(false);
        when(actorRepository.existsByNickName("testUser")).thenReturn(true);

        // when & then
        assertThrows(AlreadyNicknameException.class, () -> userService.signup(request));
        verify(actorRepository, never()).save(any(Actor.class));
    }

    @Test
    @DisplayName("이미 존재하는 전화번호로 회원가입 시도 시 예외 발생")
    void signup_WithExistingPhoneNumber() {
        // given
        UserSignupRequest request = new UserSignupRequest(
                "test@example.com",
                "Password123!",
                "01012345678",
                "Test User",
                "testUser"
        );

        when(actorRepository.existsByEmail(anyString())).thenReturn(false);
        when(actorRepository.existsByNickName(anyString())).thenReturn(false);
        when(actorRepository.existsByPhoneNum("01012345678")).thenReturn(true);

        // when & then
        assertThrows(AlreadyPhoneNumberException.class, () -> userService.signup(request));
        verify(actorRepository, never()).save(any(Actor.class));
    }

    @Test
    @DisplayName("닉네임 변경 성공 테스트")
    void changeNickname_Success() {
        // given
        String email = "test@example.com";
        String newNickname = "newNickname";
        ChangeNicknameRequest request = new ChangeNicknameRequest(newNickname);

        Actor actor = Actor.builder()
                .email(email)
                .nickName("oldNickname")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();

        when(actorRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(userRepository.existsByNickName(newNickname)).thenReturn(false);

        // when
        userService.changeNickname(request, email);

        // then
        verify(actorRepository).findByEmail(email);
        verify(userRepository).existsByNickName(newNickname);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 닉네임 변경 시도 시 예외 발생")
    void changeNickname_UserNotFound() {
        // given
        String email = "nonexistent@example.com";
        ChangeNicknameRequest request = new ChangeNicknameRequest("newNickname");

        when(actorRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.changeNickname(request, email));
        verify(userRepository, never()).existsByNickName(anyString());
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 변경 시도 시 예외 발생")
    void changeNickname_ExistingNickname() {
        // given
        String email = "test@example.com";
        String existingNickname = "existingNickname";
        ChangeNicknameRequest request = new ChangeNicknameRequest(existingNickname);

        Actor actor = Actor.builder()
                .email(email)
                .nickName("oldNickname")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();

        when(actorRepository.findByEmail(email)).thenReturn(Optional.of(actor));
        when(userRepository.existsByNickName(existingNickname)).thenReturn(true);

        // when & then
        assertThrows(AlreadyNicknameException.class, () -> userService.changeNickname(request, email));
    }

    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    void getUserInfo_Success() {
        // given
        String email = "test@example.com";
        Actor actor = Actor.builder()
                .email(email)
                .name("박찬진")
                .nickName("testNickname")
                .phoneNum("01012345678")
                .role(UserRole.USER)
                .build();

        when(actorRepository.findByEmail(email)).thenReturn(Optional.of(actor));

        // when
        UserInfoResponse response = userService.getUserInfo(email);

        // then
        assertNotNull(response);
        assertEquals(email, response.getEmail());
        assertEquals("testNickname", response.getNickname());
        assertEquals("박찬진", response.getName());
        assertEquals("01012345678", response.getPhoneNumber());
        assertEquals("USER", response.getRole());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 정보 조회 시 예외 발생")
    void getUserInfo_UserNotFound() {
        // given
        String email = "nonexistent@example.com";
        when(actorRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(email));
    }
}