package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.dto.UserCreateEditDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.filter.UserFilter;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserService userService;

    @GetMapping
    public PageResponse<UserReadDto> findAll(UserFilter userFilter, Pageable pageable) {
        Page<UserReadDto> page = userService.findAll(userFilter, pageable);

        return PageResponse.of(page);
    }

    @GetMapping("/{id}")
    public UserReadDto findById(@PathVariable("id") Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserReadDto create(@RequestBody @Validated UserCreateEditDto userCreateEditDto) {
        return userService.create(userCreateEditDto);
    }

    @PutMapping("/{id}")
    public UserReadDto update(@PathVariable("id") Long id, @RequestBody @Validated UserCreateEditDto userCreateEditDto) {
        return userService.update(id, userCreateEditDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!userService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);        }
    }
}
