package com.bufalari.payable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients; // <<<--- IMPORTAR
// import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // Se usar JpaAuditingConfig

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bufalari.payable.client") // <<<--- ADICIONAR
// @EnableJpaAuditing // Se usar JpaAuditingConfig
public class AccountsPayableServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountsPayableServiceApplication.class, args);
	}

}