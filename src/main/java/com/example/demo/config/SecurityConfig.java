package com.example.demo.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final DatabaseUserFilter databaseUserFilter;
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/user/**",
                "/create-user",
                "/create-new-user",
                "/css/styles.css",
                "/favicon.ico",
                "excel/compare",
                "/excel/get-compare")
            .permitAll()
        )
        .formLogin(form -> form
            .loginPage("/user/login")
            .defaultSuccessUrl("/excel/get-compare", true)
            .failureUrl("/user/login?error=true")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutSuccessUrl("/user/login?logout=true")
            .permitAll()
        )
        .authorizeHttpRequests(authz -> authz
            .anyRequest().authenticated()
        ).sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        ).addFilterBefore(
            databaseUserFilter, UsernamePasswordAuthenticationFilter.class
        ).cors((cors) -> cors
            .configurationSource(request -> {
              var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
              var config = new org.springframework.web.cors.CorsConfiguration();
              config.setAllowCredentials(true);
              config.setAllowedOrigins(List.of("*"));
              config.setAllowedHeaders(List.of("*"));
              config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
              source.registerCorsConfiguration("/**", config);
              return source.getCorsConfiguration(request);
            })
        )
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(16);
  }
}