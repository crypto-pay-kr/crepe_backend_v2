package dev.crepe.infra.naver.ocr.business.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class BusinessOcrResponse {
    private String registerNumber;     // 사업자등록번호
    private String corpName;           // 상호
    private String representativeName; // 대표자
    private String openDate;           // 개업일자
    private String address;            // 사업장소재지
    private String businessType;       // 업태
    private String businessItem;       // 종목
}
