package com.bufalari.payable.config;

import com.bufalari.payable.secutity.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Accounts Payable Service.
 * Configuração de segurança para o Serviço de Contas a Pagar.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    private static final String[] PUBLIC_MATCHERS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_MATCHERS).permitAll()
                // Define specific auth rules for payable endpoints / Define regras de auth específicas
                .requestMatchers(HttpMethod.GET, "/api/payables/**").hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "FINANCIAL_VIEWER") // Exemplo: Quem pode ver
                .requestMatchers(HttpMethod.POST, "/api/payables").hasAnyRole("ADMIN", "ACCOUNTANT") // Exemplo: Quem pode criar
                .requestMatchers(HttpMethod.PUT, "/api/payables/**").hasAnyRole("ADMIN", "ACCOUNTANT") // Exemplo: Quem pode atualizar tudo
                .requestMatchers(HttpMethod.PATCH, "/api/payables/**").hasAnyRole("ADMIN", "ACCOUNTANT") // Exemplo: Quem pode atualizar status/pagamento
                .requestMatchers(HttpMethod.DELETE, "/api/payables/**").hasRole("ADMIN") // Exemplo: Quem pode deletar
                .anyRequest().authenticated() // Require authentication for all others
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Restrict in production!
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}