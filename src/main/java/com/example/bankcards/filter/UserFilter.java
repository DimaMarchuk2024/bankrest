package com.example.bankcards.filter;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class UserFilter {

    String firstname;
    String lastname;
    String phoneNumber;
    String email;
    LocalDate birthDate;
    String passportNumber;
}
