package dev.crepe.domain.core.util.coin.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCoinInfo {
    private String currency;
    private String address;
    private String tag;
}
