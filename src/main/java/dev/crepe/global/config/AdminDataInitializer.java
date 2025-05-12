package dev.crepe.global.config;

import dev.crepe.domain.channel.actor.model.entity.Actor;
import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.channel.actor.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminDataInitializer {

    private final ActorRepository actorRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeAdminData() {
        return args -> {
            // 이미 admin 계정이 존재하는지 확인
            if (actorRepository.findByEmail(adminEmail).isEmpty()) {
                // admin 계정 생성
                Actor admin = Actor.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .nickName("크레페관리자")
                        .phoneNum("03212341234") // 관리자 전화번호 추가 (필요 시)
                        .name("크레페관리자")
                        .role(UserRole.ADMIN)
                        .build();

                actorRepository.save(admin);
                System.out.println("Admin 계정이 초기화되었습니다.");
            }
        };
    }
}