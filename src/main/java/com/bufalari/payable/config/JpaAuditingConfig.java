package com.bufalari.payable.config;

import com.bufalari.payable.auditing.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProviderPayable") // Ref é "auditorProviderPayable"
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProviderPayable() { // Nome do bean é "auditorProviderPayable"
        return new AuditorAwareImpl(); // Localizado em 'auditing' - OK
    }
}