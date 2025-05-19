package dev.crepe.infra.naver.ocr.id.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IdCardOcrResponse {
    private String name;
    private String personalNum;
    private String address;
    private String issueDate;
    private String authority;
}
