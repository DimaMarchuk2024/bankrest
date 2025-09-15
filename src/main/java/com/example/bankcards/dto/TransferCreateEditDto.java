package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class TransferCreateEditDto {

    Long userId;
    String cardFrom;
    String cardTo;
    LocalDate transferDate;
    BigDecimal sum;
}
