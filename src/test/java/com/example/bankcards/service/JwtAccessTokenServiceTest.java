package com.example.bankcards.service;

import com.example.bankcards.enumpack.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtAccessTokenService jwtAccessTokenService;

    @BeforeEach
    void init() {
        jwtAccessTokenService = new JwtAccessTokenService(jwtEncoder);
    }

    @Test
    void generateIdTokenSuccess() {
        UserDetails userDetails = new User("ivan@gmail.com", "123", List.of(Role.USER));
        JwtClaimsSet claimSet = JwtClaimsSet.builder()
                .claim("scope", userDetails.getAuthorities())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.MINUTES))
                .subject(userDetails.getUsername())
                .build();
        String tokenValue = "fyjfghnghn.fjgvnghgumhjm.fukfmhmvhj";
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
                null,
                userDetails.getAuthorities());
        Jwt jwt = new Jwt(tokenValue,
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.MINUTES),
                Map.of("alg", "RS256"),
                Map.of("scope", "USER"));
        doReturn(jwt).when(jwtEncoder).encode(any());

        String actualResult = jwtAccessTokenService.generateIdToken(authentication);

        assertThat(actualResult).isEqualTo(tokenValue);
    }

    @Test
    void generateIdTokenFailedIfNotCreateDUserDetailsFromAuthentication() {
        assertThrows(RuntimeException.class, () -> jwtAccessTokenService.generateIdToken(null));
    }
}