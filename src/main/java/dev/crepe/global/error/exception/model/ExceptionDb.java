package dev.crepe.global.error.exception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column
    private int code;
    
}