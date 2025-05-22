package dev.crepe.domain.auth.jwt.repository;

import dev.crepe.domain.auth.UserRole;
import dev.crepe.domain.auth.jwt.model.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<JwtToken, String> { // String으로 변경 (userEmail이 PK)

    /**
     * 이메일로 토큰 조회 (Primary Key)
     */
    Optional<JwtToken> findByUserEmail(String userEmail);

    /**
     * 특정 역할의 토큰 조회 (이메일 + 역할)
     */
    Optional<JwtToken> findByUserEmailAndRole(String userEmail, UserRole role);

    /**
     * 액세스 토큰으로 조회
     */
    Optional<JwtToken> findByAccessToken(String accessToken);

    /**
     * 리프레시 토큰으로 조회
     */
    Optional<JwtToken> findByRefreshToken(String refreshToken);

    /**
     * 사용자의 토큰 존재 여부 확인
     */
    boolean existsByUserEmail(String userEmail);

    /**
     * 특정 액세스 토큰의 존재 여부 확인
     */
    boolean existsByAccessToken(String accessToken);

    /**
     * 사용자의 기존 토큰 삭제 (로그아웃 시 사용)
     */
    @Modifying
    @Query("DELETE FROM JwtToken jt WHERE jt.userEmail = :userEmail")
    void deleteByUserEmail(@Param("userEmail") String userEmail);

    /**
     * 만료된 토큰들 일괄 삭제 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM JwtToken jt WHERE jt.updatedAt < :expiryTime")
    int deleteExpiredTokens(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * 특정 역할의 모든 토큰 조회
     */
    List<JwtToken> findAllByRole(UserRole role);

    /**
     * 특정 기간 이후 생성된 토큰들 조회
     */
    @Query("SELECT jt FROM JwtToken jt WHERE jt.createdAt >= :fromDate")
    List<JwtToken> findTokensCreatedAfter(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 사용자별 토큰 개수 조회 (중복 로그인 모니터링용)
     */
    @Query("SELECT COUNT(jt) FROM JwtToken jt WHERE jt.userEmail = :userEmail")
    int countByUserEmail(@Param("userEmail") String userEmail);

    /**
     * 특정 역할의 사용자들 토큰 조회
     */
    @Query("SELECT jt FROM JwtToken jt WHERE jt.role = :role")
    List<JwtToken> findTokensByRole(@Param("role") UserRole role);

    /**
     * 특정 시간 이후 업데이트된 토큰들 조회
     */
    @Query("SELECT jt FROM JwtToken jt WHERE jt.updatedAt >= :fromDate")
    List<JwtToken> findTokensUpdatedAfter(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 액세스 토큰 패턴으로 검색 (보안 모니터링용)
     */
    @Query("SELECT jt FROM JwtToken jt WHERE jt.accessToken LIKE :pattern")
    List<JwtToken> findByAccessTokenPattern(@Param("pattern") String pattern);
}
