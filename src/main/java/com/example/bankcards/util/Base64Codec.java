package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@UtilityClass
public class Base64Codec {

    private static final String codeWord = "bank_rest";

    public static String encodeCardNumber(String cardNumber) {
        byte[] cardNumberBytes = cardNumber.getBytes(StandardCharsets.UTF_8);
        xorInPlace(cardNumberBytes, codeWord.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(cardNumberBytes);
    }

    public static String decodeCardNumber(String cardNumberBase64) {
        byte[] cardNumberBytes = Base64.getDecoder().decode(cardNumberBase64);
        xorInPlace(cardNumberBytes, codeWord.getBytes(StandardCharsets.UTF_8));

        return new String(cardNumberBytes, StandardCharsets.UTF_8);
    }

    private static void xorInPlace(byte[] data, byte[] key) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= key[i % key.length];
        }
    }
}
