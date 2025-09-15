package com.example.bankcards.dto;

import com.example.bankcards.enumpack.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class UserReadDto {

    Long id;
    String firstname;
    String lastname;
    String phoneNumber;
    String email;
    Role role;
    LocalDate birthDate;
    String passportNumber;
}

