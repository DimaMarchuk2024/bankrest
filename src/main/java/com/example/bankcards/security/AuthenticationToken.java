package com.example.bankcards.security;

import com.example.bankcards.dto.LoginDto;
import com.example.bankcards.service.JwtAccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationToken {

    private final AuthenticationManager authenticationManager;
    private final JwtAccessTokenService jwtAccessTokenService;

    public AccessToken authenticate(LoginDto loginDto) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
                loginDto.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        String idToken = jwtAccessTokenService.generateIdToken(authenticate);

        return new AccessToken(idToken);
    }
}
