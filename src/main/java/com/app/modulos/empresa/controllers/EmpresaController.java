package com.app.modulos.empresa.controllers;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.services.EmpresaService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.usuario.security.UserPrincipal;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
	private final EmpresaService empresaService;

	public EmpresaController(EmpresaService empresaService) {
		this.empresaService = empresaService;
	}

	@GetMapping
	public ResponseEntity<List<Empresa>> list() {
		return ResponseEntity.ok(empresaService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Empresa> get(@PathVariable Long id) {
		return empresaService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Empresa> create(
		@RequestBody Empresa empresa,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Empresa saved = empresaService.save(empresa);
		if (principal != null) {
			empresaService.assignEmpresaToUser(saved.getId(), principal.getUsername());
		}
		return ResponseEntity.ok(saved);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Empresa> update(@PathVariable Long id, @RequestBody Empresa empresa) {
		return empresaService.update(id, empresa)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Empresa> disable(@PathVariable Long id) {
		return empresaService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
