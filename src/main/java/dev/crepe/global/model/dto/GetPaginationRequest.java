package dev.crepe.global.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// pagination 공통 요청 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPaginationRequest {
    private String authEmail;
    private int page;
    private int size;
}
