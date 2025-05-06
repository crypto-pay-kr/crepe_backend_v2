package dev.crepe.infra.sms.util;

import java.util.Random;

public class CodeGenerator {
    private static final Random RANDOM = new Random();

    public static String generateCode(int digitCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digitCount; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}