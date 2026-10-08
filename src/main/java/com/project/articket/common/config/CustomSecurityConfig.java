package com.project.articket.common.config;

import com.project.articket.common.filter.DeactiveAccessFilter;
import com.project.articket.common.filter.JWTCheckFilter;
import com.project.articket.common.filter.WithdrawAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class CustomSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        authenticationProvider.setPasswordEncoder(
                passwordEncoder
        );

        return new ProviderManager(
                authenticationProvider
        );
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JWTCheckFilter jwtCheckFilter,
            WithdrawAccessFilter withdrawAccessFilter,
            DeactiveAccessFilter deactiveAccessFilter
    ) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(form ->
                        form.disable()
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/**"
                        ).permitAll()

                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/exhibitions/**",
                                "/api/venues/**",
                                "/api/reviews/**",
                                "/api/asks/**",
                                "/api/images/**",
                                "/upload/review/**",
                                "/api/wishes/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/staff/**"
                        ).hasRole("STAFF")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtCheckFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterAfter(
                        withdrawAccessFilter,
                        JWTCheckFilter.class
                )

                .addFilterAfter(
                        deactiveAccessFilter,
                        WithdrawAccessFilter.class
                );

        return http.build();
    }
}