package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.Permiso;
import com.app.modulos.usuario.services.PermisoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.modulos.config.RequiresFeature;
import com.app.modulos.config.Auditable;

@RestController
@RequestMapping("/api/permisos")
@RequiresFeature("roles-permisos")
public class PermisoController {
	private final PermisoService permisoService;

	public PermisoController(PermisoService permisoService) {
		this.permisoService = permisoService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	public ResponseEntity<List<Permiso>> list() {
		return ResponseEntity.ok(permisoService.findAll());
	}

	@PostMapping
	@PreAuthorize("hasRole('SUPERADMIN')")
	@Auditable(accion = "CREAR", modulo = "ROLES_PERMISOS")
	public ResponseEntity<Permiso> create(@RequestBody Permiso permiso) {
		return ResponseEntity.ok(permisoService.save(permiso));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	@Auditable(accion = "EDITAR", modulo = "ROLES_PERMISOS")
	public ResponseEntity<Permiso> update(@PathVariable Long id, @RequestBody Permiso details) {
		return ResponseEntity.ok(permisoService.update(id, details));
	}
}
