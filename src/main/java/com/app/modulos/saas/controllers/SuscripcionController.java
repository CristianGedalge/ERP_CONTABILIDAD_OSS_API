package com.app.modulos.saas.controllers;

import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.services.SuscripcionService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {
	private final SuscripcionService suscripcionService;

	public SuscripcionController(SuscripcionService suscripcionService) {
		this.suscripcionService = suscripcionService;
	}

	@GetMapping
	public ResponseEntity<List<Suscripcion>> list() {
		return ResponseEntity.ok(suscripcionService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Suscripcion> get(@PathVariable Long id) {
		return suscripcionService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Suscripcion> create(@RequestBody Suscripcion suscripcion) {
		return ResponseEntity.ok(suscripcionService.save(suscripcion));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Suscripcion> update(@PathVariable Long id, @RequestBody Suscripcion suscripcion) {
		return suscripcionService.update(id, suscripcion)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Suscripcion> disable(@PathVariable Long id) {
		return suscripcionService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
