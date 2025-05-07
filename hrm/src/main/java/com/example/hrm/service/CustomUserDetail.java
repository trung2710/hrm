package com.example.hrm.service;

import com.example.hrm.domain.NhanVien;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail implements UserDetails {
    private final NhanVien nhanVien;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetail(NhanVien user, Collection<? extends GrantedAuthority> authorities) {
        this.nhanVien = user;
        this.authorities = authorities;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return nhanVien.getPassword();
    }

    @Override
    public String getUsername() {
        return nhanVien.getEmail();
    }



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
