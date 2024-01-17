package com.website.util;

import java.util.Random;

/**
 * @Author: ahl
 * @Date: 2023/3/24 21:07
 */

public class VerificationCodeGenerator {
    private static Random random = new Random();

    public static String generateCode(int digit) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < digit; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    private VerificationCodeGenerator(){

    }
}
