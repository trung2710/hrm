package com.example.hrm.config;

import com.example.hrm.repository.UserRepository;
import com.example.hrm.service.CustomUserDetailsService;
import com.example.hrm.service.UserService;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
//        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;

    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                DispatcherType.INCLUDE) .permitAll()
                        .requestMatchers("/login", "/css/**", "/js/**",
                                "/images/**").permitAll()
                        .requestMatchers("/api/ai-chat/**").permitAll()
                        .requestMatchers("/chat-demo").permitAll()
                        .requestMatchers("/chat-test").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf
                        // Disable CSRF cho API endpoints
                        .ignoringRequestMatchers("/api/ai-chat/**")
                )
                .cors(cors -> cors.configure(http))
                .formLogin(formLogin -> formLogin
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/", true)
                        .permitAll() // Sử dụng trang đăng nhập mặc định
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
        ;
        return http.build();
    }
}
