package com.example.bankcards.exception;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ErrorResponseDto {

    String message;
    String detailMessage;
    LocalDateTime errorTime;
}
