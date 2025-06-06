package com.miempresa.erp.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class CustomUserDetails extends User {

    private Long id;
    private String userType;

    public CustomUserDetails(
        Long id,
        String username,
        String password,
        String userType,
        Collection<? extends GrantedAuthority> authorities,
        boolean enabled
    ) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.userType = userType;
    }

    public Long getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }
}
