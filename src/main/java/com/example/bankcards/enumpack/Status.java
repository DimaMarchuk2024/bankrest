package com.example.bankcards.enumpack;

import org.springframework.security.core.GrantedAuthority;

public enum Status implements GrantedAuthority {
    ACTIVE, BLOCKED, EXPIRED;

    @Override
    public String getAuthority() {
        return name();
    }
}