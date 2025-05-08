package dev.crepe.infra.naver.id_ocr.entity.dto;


import lombok.Data;

@Data
public class IdCardOcrResponse {

    private String name;
    private String personalNum;
    private String address;
    private String issueDate;
    private String authority;
}
