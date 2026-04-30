package com.trackfindergarage.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación backend.
 *
 * <p>Arranca el contexto de Spring Boot y registra la configuración de la API, la seguridad y la
 * persistencia necesarias para ejecutar el sistema.</p>
 */
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
