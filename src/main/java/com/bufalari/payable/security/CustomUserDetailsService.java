package com.bufalari.payable.security;

import com.bufalari.payable.client.AuthServiceClient; // Correct package
import com.bufalari.payable.dto.UserDetailsDTO;      // Correct package
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Service to load user-specific data by calling the Authentication Service.
 * Serviço para carregar dados específicos do usuário chamando o Serviço de Autenticação.
 */
@Service("payableUserDetailsService") // Specific bean name
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final AuthServiceClient authServiceClient;

    public CustomUserDetailsService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("[Payable] Attempting to load user details for username: {}", username);
        UserDetailsDTO userDetailsDTO;
        try {
            userDetailsDTO = authServiceClient.getUserByUsername(username);

            if (userDetailsDTO == null) {
                log.warn("[Payable] User details DTO is null for username: {}", username);
                throw new UsernameNotFoundException("User not found via auth service: " + username);
            }

            log.info("[Payable] Successfully loaded user details via auth service for username: {}", username);
            return new User(
                    userDetailsDTO.getUsername(),
                    userDetailsDTO.getPassword() != null ? userDetailsDTO.getPassword() : "",
                    userDetailsDTO.getRoles() != null ?
                        userDetailsDTO.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            .collect(Collectors.toList())
                        : Collections.emptyList()
            );

        } catch (FeignException.NotFound e) {
            log.warn("[Payable] User not found via auth service for username: {}. Feign status: 404", username);
            throw new UsernameNotFoundException("User not found: " + username, e);
        } catch (FeignException e) {
            log.error("[Payable] Feign error calling auth service for username: {}. Status: {}, Message: {}", username, e.status(), e.getMessage());
            throw new UsernameNotFoundException("Failed to load user details (auth service comm error) for user: " + username, e);
        } catch (Exception e) {
            log.error("[Payable] Unexpected error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("Unexpected error loading user details for user: " + username, e);
        }
    }
}