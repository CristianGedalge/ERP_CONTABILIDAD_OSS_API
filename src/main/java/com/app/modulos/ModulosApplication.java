package com.app.modulos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ModulosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModulosApplication.class, args);
	}

}
