package com.bufalari.payable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import and enable Auditing via config class / Importar e habilitar Auditoria via classe de config
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Accounts Payable Service.
 * Classe principal da aplicação para o Serviço de Contas a Pagar.
 */
@SpringBootApplication
// Auditing is enabled via JpaAuditingConfig / Auditoria habilitada via JpaAuditingConfig
// @EnableJpaAuditing // Remove if using JpaAuditingConfig
public class AccountsPayableServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountsPayableServiceApplication.class, args);
	}

}