// Path: src/main/java/com/bufalari/payable/dto/UserDetailsDTO.java
package com.bufalari.payable.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for user details received from the authentication service.
 * DTO para os detalhes do usuário recebidos do serviço de autenticação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private UUID id;
    private String username;
    private String password;
    private List<String> roles;
}