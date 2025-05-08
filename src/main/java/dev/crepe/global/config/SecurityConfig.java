package dev.crepe.global.config;

import dev.crepe.domain.auth.jwt.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_URI = {
            "/user/signup",
            "/store/signup",
            "/login",
            "/sms/**",
            "/ocr/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/api/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/captcha"
    };

    private static final String[] ADMIN_URI = {
            "/admin/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // JWT 필터를 래핑하여 PUBLIC_URI 경로에 대해 인증 스킵
        Filter skipAuthenticationFilter = createSkipAuthenticationFilter(jwtAuthenticationFilter);

        http
                .authorizeHttpRequests(
                        req -> req
                                .requestMatchers(PUBLIC_URI).permitAll()
                                .requestMatchers(ADMIN_URI).hasAuthority("ADMIN")
                                .requestMatchers("/upbit/**").permitAll()
                                .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PUBLIC_URI)
                        .disable())
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                                })
                )
                .addFilterBefore(skipAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * PUBLIC_URI 경로에 대해 인증을 스킵하는 필터를 생성합니다.
     * 이 필터는 JwtAuthenticationFilter를 내부에서 호출하지만,
     * PUBLIC_URI 경로에 대해서는 JwtAuthenticationFilter를 호출하지 않고 다음 필터로 넘깁니다.
     */
    private Filter createSkipAuthenticationFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain) throws ServletException, IOException {

                String path = request.getRequestURI();

                // PUBLIC_URI 경로인 경우 JWT 인증 필터를 스킵하고 다음 필터로 진행
                if (shouldSkipAuthentication(path)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // PUBLIC_URI가 아닌 경우 JWT 인증 필터 실행
                jwtAuthenticationFilter.doFilter(request, response, filterChain);
            }

            private boolean shouldSkipAuthentication(String path) {
                return Arrays.stream(PUBLIC_URI).anyMatch(uri -> {
                    // 와일드카드(**) 처리
                    if (uri.endsWith("/**")) {
                        String prefix = uri.substring(0, uri.length() - 3);
                        return path.startsWith(prefix);
                    }
                    return path.startsWith(uri);
                });
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}