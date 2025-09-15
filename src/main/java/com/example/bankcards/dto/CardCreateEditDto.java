package com.example.bankcards.dto;

import com.example.bankcards.enumpack.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class CardCreateEditDto {

    String number;
    Long userId;
    LocalDate expirationDate;
    Status status;
    BigDecimal balance;
}

