package com.app.modulos.saas.controllers;

import com.app.modulos.saas.entities.Configuracion;
import com.app.modulos.saas.services.ConfiguracionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuraciones")
public class ConfiguracionController {
	private final ConfiguracionService configuracionService;

	public ConfiguracionController(ConfiguracionService configuracionService) {
		this.configuracionService = configuracionService;
	}

	@GetMapping
	public ResponseEntity<List<Configuracion>> list() {
		return ResponseEntity.ok(configuracionService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Configuracion> get(@PathVariable Long id) {
		return configuracionService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Configuracion> create(@RequestBody Configuracion configuracion) {
		return ResponseEntity.ok(configuracionService.save(configuracion));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Configuracion> update(@PathVariable Long id, @RequestBody Configuracion configuracion) {
		return configuracionService.update(id, configuracion)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
