// Path: src/main/java/com/bufalari/payable/client/AuthServiceClient.java
package com.bufalari.payable.client;

import com.bufalari.payable.dto.UserDetailsDTO; // Correct package
import org.springframework.cloud.openfeign.EnableFeignClients; // Import EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communication with the authentication service.
 * Cliente Feign para comunicação com o serviço de autenticação.
 */
@FeignClient(name = "auth-service-client-payable", url = "${auth.service.url}")
public interface AuthServiceClient {

    /**
     * Retrieves user details by username from the authentication service.
     * Busca os detalhes do usuário por nome de usuário no serviço de autenticação.
     */
    @GetMapping("/api/users/username/{username}") // VERIFY THIS ENDPOINT
    UserDetailsDTO getUserByUsername(@PathVariable("username") String username);

     /**
     * Retrieves user details by UUID from the authentication service.
     * Busca os detalhes do usuário por UUID no serviço de autenticação.
     */
     @GetMapping("/api/users/{id}") // VERIFY THIS ENDPOINT
     UserDetailsDTO getUserById(@PathVariable("id") String userId);
}