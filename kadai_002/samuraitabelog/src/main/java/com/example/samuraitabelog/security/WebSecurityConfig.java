package com.example.samuraitabelog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests                
                .requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/", "/restaurants", "/restaurants/{id}", "/password/**", "/restaurants/{id}/reviews", "/stripe/webhook").permitAll()  // すべてのユーザーにアクセスを許可するURL           
                .requestMatchers("/signup/**").anonymous()
                .requestMatchers("/subscribe/confirm", "/subscribe/upgraded").hasRole("GENERAL_FREE")
                .requestMatchers("/subscribe/customer", "/subscribe/complete", "/subscribe/cancel", "/reviews/**" ,"/favorites/**", "/reservations/**", "/restaurants/{id}/reviews/**", "/restaurants/{id}/reservations/**").hasRole("GENERAL_PAID")
                .requestMatchers("/user/**").hasAnyRole("GENERAL_FREE", "GENERAL_PAID", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")  // 管理者にのみアクセスを許可するURL
                .anyRequest().authenticated()                   // 上記以外のURLはログインが必要（会員または管理者のどちらでもOK）
            )
            .formLogin((form) -> form
                .loginPage("/login")              // ログインページのURL
                .loginProcessingUrl("/login")     // ログインフォームの送信先URL
                .defaultSuccessUrl("/?loggedIn")  // ログイン成功時のリダイレクト先URL
                .failureUrl("/login?error")       // ログイン失敗時のリダイレクト先URL
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/?loggedOut")  // ログアウト時のリダイレクト先URL
                .permitAll()
            )  
        	.csrf(csrf -> csrf.ignoringRequestMatchers("/stripe/webhook"));
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
