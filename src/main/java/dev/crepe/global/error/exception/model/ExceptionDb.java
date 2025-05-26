package dev.crepe.global.error.exception.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exception_db")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // 또는 EnumType.ORDINAL
    private ExceptionStatus status;
    
    @Column(nullable = false)
    private String message;
    
    // code는 status에서 가져올 수 있으므로 별도 필드가 필요 없다면 제거 가능
    // 필요하다면 아래와 같이 메서드로 제공
    public int getCode() {
        return status.getCode();
    }
}