package com.oficinamecanica.OficinaMecanica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OficinaMecanicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OficinaMecanicaApplication.class, args);
		System.out.println("Documentação API: http://localhost:8084/swagger-ui.html");
		System.out.println("Autenticação: JWT + OAuth2 (Google)");
	}

}
