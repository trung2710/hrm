package com.example.hrm.service;

import com.example.hrm.domain.NhanVien;
import com.example.hrm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //logic
        NhanVien nhanVien=this.userRepository.findByEmail(username);
        if(nhanVien==null){
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomUserDetail(nhanVien,nhanVien.getEmail(),nhanVien.getPassword(),Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+nhanVien.getChucVu().getQuyen().getTenQuyen())));
    }
}
