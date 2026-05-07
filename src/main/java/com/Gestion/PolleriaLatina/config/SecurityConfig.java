package com.Gestion.PolleriaLatina.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.csrf(csrf -> csrf.disable());

    http.authorizeHttpRequests(auth -> {
      auth.requestMatchers("/assets/**", "/login", "/forgot-password", "/reset-password", "/error").permitAll();
      auth.anyRequest().authenticated();
    });

    http.formLogin(form -> {
      form.loginPage("/login");
      form.defaultSuccessUrl("/", true);
      form.failureUrl("/login?error=Credenciales_incorrectas");
      form.permitAll();
    });
    http.rememberMe(remember -> {
      remember.key("PolleriaLatinaSuperSecreta2026");
      remember.tokenValiditySeconds(86400 * 30);
      remember.rememberMeParameter("remember-me");
    });

    http.logout(logout -> {
      logout.logoutUrl("/logout");
      logout.logoutSuccessUrl("/login?logout");
      logout.deleteCookies("JSESSIONID", "remember-me");
    });

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}