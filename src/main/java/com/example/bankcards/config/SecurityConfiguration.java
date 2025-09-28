package com.example.bankcards.config;

import com.example.bankcards.enumpack.Role;
import com.example.bankcards.security.RsaKeys;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final RsaKeys rsaKeys;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/transfers/{id}").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/transfers/{id}").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/{userId}/transfers").hasAuthority(Role.USER.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/v1/transfers").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cards/{id}").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cards/{id}").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/v1/cards").hasAuthority(Role.ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/api/v1/cards").hasAuthority(Role.ADMIN.getAuthority())
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2ResourceServer ->  oauth2ResourceServer.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.rsaPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(jwkSource());
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAPublicKey publicKey = rsaKeys.rsaPublicKey();
        RSAPrivateKey privateKey = rsaKeys.rsaPrivateKey();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }
}


