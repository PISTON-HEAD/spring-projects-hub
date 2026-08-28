package com.ragapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Separated from SecurityConfig to break the circular dependency:
 *   SecurityConfig → JwtAuthenticationFilter → UserDetailsService → SecurityConfig
 *
 * All user identity beans live here; SecurityConfig only handles the filter chain.
 */
@Configuration
public class UserConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Platform administrator (actuator access, not tied to a tenant org).
        var admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        // Organization members share one knowledge base (see OrgAccountService).
        // ORG_ADMIN can upload documents; ORG_MEMBER is read-only (ask questions only).
        var acmeAdmin = User.builder()
                .username("acme-admin")
                .password(passwordEncoder.encode("acme-admin123"))
                .roles("ORG_ADMIN")
                .build();

        var acme = User.builder()
                .username("acme")
                .password(passwordEncoder.encode("acme123"))
                .roles("ORG_MEMBER")
                .build();

        var globexAdmin = User.builder()
                .username("globex-admin")
                .password(passwordEncoder.encode("globex-admin123"))
                .roles("ORG_ADMIN")
                .build();

        var globex = User.builder()
                .username("globex")
                .password(passwordEncoder.encode("globex123"))
                .roles("ORG_MEMBER")
                .build();

        return new InMemoryUserDetailsManager(admin, acmeAdmin, acme, globexAdmin, globex);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
