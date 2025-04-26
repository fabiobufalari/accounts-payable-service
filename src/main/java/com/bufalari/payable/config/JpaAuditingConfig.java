// Path: src/main/java/com/bufalari/payable/config/JpaAuditingConfig.java
package com.bufalari.payable.config;

import com.bufalari.payable.auditing.AuditorAwareImpl; // Import implementation
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing.
 * Configuração para Auditoria JPA.
 */
@Configuration
// Enable auditing and specify the AuditorAware bean name
// Habilita auditoria e especifica o nome do bean AuditorAware
@EnableJpaAuditing(auditorAwareRef = "auditorProviderPayable") // Use unique bean name
public class JpaAuditingConfig {

    /**
     * Provides the AuditorAware bean implementation.
     * Fornece a implementação do bean AuditorAware.
     * @return An instance of AuditorAware<String>.
     */
    @Bean
    public AuditorAware<String> auditorProviderPayable() { // Bean name matches ref
        return new AuditorAwareImpl();
    }
}