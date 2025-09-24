package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateEditDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.filter.UserFilter;
import com.example.bankcards.mapper.UserCreateEditMapper;
import com.example.bankcards.mapper.UserReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

import static com.example.bankcards.entity.QUser.user;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserReadMapper userReadMapper;
    private final UserCreateEditMapper userCreateEditMapper;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public Page<UserReadDto> findAll(UserFilter userFilter, Pageable pageable) {
        Predicate predicate = QPredicate.builder()
                .add(userFilter.getFirstname(), user.firstname::containsIgnoreCase)
                .add(userFilter.getLastname(), user.lastname::containsIgnoreCase)
                .add(userFilter.getPhoneNumber(), user.phoneNumber::containsIgnoreCase)
                .add(userFilter.getEmail(), user.email::containsIgnoreCase)
                .add(userFilter.getBirthDate(), user.birthDate::before)
                .add(userFilter.getPassportNumber(), user.passportNumber::containsIgnoreCase)
                .buildAnd();

        return userRepository.findAll(predicate, pageable)
                .map(userReadMapper::map);
    }

    public UserReadDto findById(Long id) {
        return userRepository.findById(id)
                .map(userReadMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id = " + id));
    }

    @Transactional
    public UserReadDto create(UserCreateEditDto userCreateEditDto) {
        return Optional.of(userCreateEditDto)
                .map(userCreateEditMapper::map)
                .map(userRepository::save)
                .map(userReadMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Failed to create user"));
    }

    @Transactional
    public UserReadDto update(Long id, UserCreateEditDto userCreateEditDto) {
        User userForUpdate = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id = " + id));

        return Optional.of(userForUpdate)
                .map(user -> userCreateEditMapper.map(userCreateEditDto, user))
                .map(userRepository::saveAndFlush)
                .map(userReadMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Failed to update the user with Id = " + id));
    }

    @Transactional
    public boolean delete(Long id) {
        return Optional.ofNullable(userRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found user with id = " + id)))
                .map(user -> {
                    userRepository.delete(user);
                    userRepository.flush();
                    log.info("User with id = " + id + " deleted");
                    return true;
                })
                .orElse(false);
    }

    public String verify(String email, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(email);
        }
        return "fail";
    }
}
