package com.bancoxyz.banco_microservicio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(h -> h.frameOptions(f -> f.disable()))
            .authorizeHttpRequests(auth -> auth
                // Solo health e info son públicos (necesarios para monitoreo)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/**").hasAuthority("SCOPE_read")
                .requestMatchers(HttpMethod.POST, "/**").hasAuthority("SCOPE_write")
                .anyRequest().hasAuthority("SCOPE_write")
            )
            // Validación de JWT de OAuth2
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}