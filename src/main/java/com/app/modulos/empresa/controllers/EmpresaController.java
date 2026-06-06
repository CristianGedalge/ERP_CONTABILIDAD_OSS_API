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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.usuario.security.UserPrincipal;

import com.app.modulos.config.RequiresFeature;

@RestController
@RequestMapping("/api/empresas")
@RequiresFeature("mi-empresa")
public class EmpresaController {
	private final EmpresaService empresaService;

	public EmpresaController(EmpresaService empresaService) {
		this.empresaService = empresaService;
	}

	@GetMapping
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<List<Empresa>> list() {
		return ResponseEntity.ok(empresaService.findAll());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasAuthority('PERM_EMPRESA_READ')")
	public ResponseEntity<Empresa> get(
		@PathVariable Long id,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		// Seguridad: Si no es SUPERADMIN, solo puede ver SU empresa
		if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			if (!id.equals(principal.getEmpresaId())) {
				return ResponseEntity.status(403).build(); // Acceso Denegado
			}
		}
		
		return empresaService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Empresa> create(
		@RequestBody Empresa empresa,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Empresa saved = empresaService.save(empresa);
		return ResponseEntity.ok(saved);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN') or hasAuthority('PERM_EMPRESA_WRITE')")
	public ResponseEntity<Empresa> update(
		@PathVariable Long id, 
		@RequestBody Empresa empresa,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		// Seguridad: Si no es SUPERADMIN, solo puede editar SU empresa
		if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			if (!id.equals(principal.getEmpresaId())) {
				return ResponseEntity.status(403).build(); // Acceso Denegado
			}
		}

		return empresaService.update(id, empresa)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Empresa> disable(@PathVariable Long id) {
		return empresaService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
