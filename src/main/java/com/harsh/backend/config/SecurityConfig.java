package com.harsh.backend.config;

import com.harsh.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions/topics").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/questions/*/submit").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/generate").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/interview/leaderboard").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/code/run").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/generate/coding").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/review").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/mock-interview/start").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/mock-interview/evaluate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/mock-interview/summary").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/chat").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/resume/analyze").permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}