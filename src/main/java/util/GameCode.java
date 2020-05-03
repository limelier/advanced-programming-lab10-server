package util;

import java.util.Random;

public class GameCode {
    private static final Random rng = new Random();

    public static String generate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(randomDigit());
        }
        return code.toString();
    }

    public static int randomDigit() {
        return rng.nextInt(9) + 1;
    }
}
