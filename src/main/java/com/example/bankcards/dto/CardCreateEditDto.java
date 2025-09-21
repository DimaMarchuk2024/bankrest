package com.example.bankcards.dto;

import com.example.bankcards.enumpack.Status;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class CardCreateEditDto {

    @NotBlank
    @Size(min = 16, max = 16)
    String number;

    @NotNull
    Long userId;

    @Future
    @NotNull
    LocalDate expirationDate;

    @NotNull
    Status status;

    @PositiveOrZero
    @NotNull
    @Digits(integer = 100000000, fraction = 2)
    BigDecimal balance;
}

