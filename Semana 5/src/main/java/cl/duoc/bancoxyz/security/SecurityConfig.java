package cl.duoc.bancoxyz.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/bff/web/**").hasRole("WEB")
                .requestMatchers("/api/v1/bff/mobile/**").hasRole("MOBILE")
                .requestMatchers("/api/v1/bff/atm/**").hasRole("ATM")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails webUser = User.builder()
                .username("web_user")
                .password("{noop}web123")
                .roles("WEB")
                .build();

        UserDetails mobileUser = User.builder()
                .username("mobile_user")
                .password("{noop}mobile123")
                .roles("MOBILE")
                .build();

        UserDetails atmUser = User.builder()
                .username("atm_user")
                .password("{noop}atm123")
                .roles("ATM")
                .build();

        return new InMemoryUserDetailsManager(webUser, mobileUser, atmUser);
    }
}