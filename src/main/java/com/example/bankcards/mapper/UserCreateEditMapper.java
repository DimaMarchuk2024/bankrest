package com.example.bankcards.mapper;

import com.example.bankcards.dto.UserCreateEditDto;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserCreateEditMapper implements Mapper<UserCreateEditDto, User> {

    @Override
    public User map(UserCreateEditDto userCreateEditDto) {
        User user = new User();
        copy(userCreateEditDto, user);
        return user;
    }

    @Override
    public User map(UserCreateEditDto userCreateEditDto, User user) {
        copy(userCreateEditDto, user);
        return user;
    }

    private void copy(UserCreateEditDto userCreateEditDto, User user) {
        user.setFirstname(userCreateEditDto.getFirstname());
        user.setLastname(userCreateEditDto.getLastname());
        user.setPhoneNumber(userCreateEditDto.getPhoneNumber());
        user.setEmail(userCreateEditDto.getEmail());
        user.setRole(userCreateEditDto.getRole());
        user.setBirthDate(userCreateEditDto.getBirthDate());
        user.setPassportNumber(userCreateEditDto.getPassportNumber());
        user.setPassword(userCreateEditDto.getPassword());
    }
}
