package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.InfoUsuario;
import com.app.modulos.usuario.services.InfoUsuarioService;
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
@RequestMapping("/api/info-usuario")
public class InfoUsuarioController {
	private final InfoUsuarioService infoUsuarioService;

	public InfoUsuarioController(InfoUsuarioService infoUsuarioService) {
		this.infoUsuarioService = infoUsuarioService;
	}

	@GetMapping
	public ResponseEntity<List<InfoUsuario>> list() {
		return ResponseEntity.ok(infoUsuarioService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<InfoUsuario> get(@PathVariable Long id) {
		return infoUsuarioService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<InfoUsuario> create(@RequestBody InfoUsuario infoUsuario) {
		return ResponseEntity.ok(infoUsuarioService.save(infoUsuario));
	}

	@PutMapping("/{id}")
	public ResponseEntity<InfoUsuario> update(@PathVariable Long id, @RequestBody InfoUsuario infoUsuario) {
		return infoUsuarioService.update(id, infoUsuario)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<InfoUsuario> delete(@PathVariable Long id) {
		return infoUsuarioService.delete(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
