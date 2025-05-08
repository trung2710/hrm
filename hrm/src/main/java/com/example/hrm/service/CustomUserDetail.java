package com.example.hrm.service;

import com.example.hrm.domain.NhanVien;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail extends User {

    private NhanVien nhanVien;

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public CustomUserDetail(NhanVien nhanVien,String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.nhanVien=nhanVien;
    }

    public CustomUserDetail(NhanVien nhanVien,String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.nhanVien=nhanVien;
    }
}
