package com.example.bankcards.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class TransferCreateEditDto {

    @NotNull
    Long userId;

    @NotBlank
    @Size(min = 16, max = 16)
    String cardFrom;

    @NotBlank
    @Size(min = 16, max = 16)
    String cardTo;

    LocalDate transferDate = LocalDate.now();

    @Positive
    @NotNull
    @Digits(integer = 100000, fraction = 2)
    BigDecimal sum;
}
