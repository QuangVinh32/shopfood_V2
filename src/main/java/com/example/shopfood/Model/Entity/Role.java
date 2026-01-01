package com.example.shopfood.Model.Entity;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN,
    MANAGER,
    USER;

    public String getAuthority() {
        return this.name();
    }
}