package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginDto;
import com.example.bankcards.security.AccessToken;
import com.example.bankcards.security.AuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/authentication")
public class AuthenticationController {

    private final AuthenticationToken authenticationToken;

    @PostMapping()
    public AccessToken getToken(@RequestBody @Validated LoginDto loginDto) {
        return authenticationToken.authenticate(loginDto);
    }
}
