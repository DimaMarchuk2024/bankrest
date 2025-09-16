package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements Mapper<User, UserReadDto> {

    @Override
    public UserReadDto map(User user) {
        return new UserReadDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole(),
                user.getBirthDate(),
                user.getPassportNumber()
        );
    }
}