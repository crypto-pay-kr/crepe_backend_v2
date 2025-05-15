package dev.crepe.domain.core.account.util;

import dev.crepe.domain.bank.model.entity.Bank;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GenerateAccountAddress {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String generate(Bank bank) {
        String bankCode = bank.getBankCode();
        int totalLength = 16;

        // 나머지 자리 수만큼 무작위 문자 생성
        List<Character> characters = new ArrayList<>();

        for (int i = 0; i < totalLength - bankCode.length(); i++) {
            int index = (int) (Math.random() * CHARACTERS.length());
            characters.add(CHARACTERS.charAt(index));
        }

        // bankCode 문자도 하나씩 넣기
        for (char c : bankCode.toCharArray()) {
            characters.add(c);
        }

        // 전체를 셔플해서 bankCode가 랜덤 위치에 섞이도록
        Collections.shuffle(characters);

        // StringBuilder로 결과 생성
        StringBuilder sb = new StringBuilder();
        for (char c : characters) {
            sb.append(c);
        }

        return sb.toString();
    }
}
