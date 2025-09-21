package com.example.bankcards.dto;

import com.example.bankcards.enumpack.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@Value
public class UserCreateEditDto {

    @NotBlank
    @Size(min = 2, max = 64)
    String firstname;

    @NotBlank
    @Size(min = 2, max = 64)
    String lastname;

    @NotBlank
    String phoneNumber;

    @Email
    String email;

    @NotNull
    Role role;

    @NotNull
    @Past
    LocalDate birthDate;

    @NotBlank
    @Size(min = 9, max = 9)
    String passportNumber;

    @NotBlank
    @Size(min = 3, max = 64)
    String password;
}