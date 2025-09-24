package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BankCardMask {

    public static final String PATTERN = "(\\d{4})(\\d{4})(\\d{4})(\\d{4})";
    public static final String REPLACE = "**** **** **** $4";

    public static String getNumberCardMask(String numberCard) {
        return numberCard.replaceAll(PATTERN, REPLACE);
    }

}
