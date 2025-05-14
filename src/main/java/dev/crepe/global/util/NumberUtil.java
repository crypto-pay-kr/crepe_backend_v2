package dev.crepe.global.util;

public class NumberUtil {
    private NumberUtil() {
        // private 생성자
    }

    public static String removeDash(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("-", "");
    }
}