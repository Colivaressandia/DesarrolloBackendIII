package com.bancoxyz.cuentas_api.config;

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
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/**")
                .hasAnyAuthority("SCOPE_read", "SCOPE_web.read", "SCOPE_mobile.read", "SCOPE_atm.read")
                .requestMatchers(HttpMethod.POST, "/api/v1/cuentas-bancarias/transferencias")
                .hasAnyAuthority("SCOPE_write", "SCOPE_atm.write")
                .requestMatchers(HttpMethod.POST, "/**").hasAuthority("SCOPE_write")
                .anyRequest().hasAuthority("SCOPE_write")
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}