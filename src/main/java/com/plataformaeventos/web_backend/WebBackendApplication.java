package com.plataformaeventos.web_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada principal de la aplicación Spring Boot.
 *
 * Se configuran explícitamente:
 *  - El paquete donde se encuentran las entidades JPA.
 *  - El paquete donde se encuentran los repositorios JPA.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.plataformaeventos.web_backend.repository")
@EntityScan(basePackages = "com.plataformaeventos.web_backend.model")
public class WebBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebBackendApplication.class, args);
	}
}