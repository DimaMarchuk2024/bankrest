package com.example.bankcards.filter;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class CardFilter {
    String number;
    LocalDate expirationDate;
}
