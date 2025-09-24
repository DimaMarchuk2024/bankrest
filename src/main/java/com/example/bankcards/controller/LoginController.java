package com.example.bankcards.controller;

import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/login")
public class LoginController {

    private final UserService userService;

    @PostMapping()
    public String login(String email, String password) {
        return userService.verify(email, password);
    }
}
