package com.example.bankcards.filter;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class TransferFilter {

    String cardFromDto;
    String cardToDto;
    LocalDate transferDate;
}
