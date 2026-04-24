package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.services.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_READ')")
	public ResponseEntity<List<Rol>> list(HttpServletRequest request) {
    Long empresaId = (Long) request.getAttribute("empresaId");
		return ResponseEntity.ok(roleService.findAllByEmpresa(empresaId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_READ')")
	public ResponseEntity<Rol> get(@PathVariable Long id) {
		return roleService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	public ResponseEntity<Rol> create(@RequestBody Rol rol, HttpServletRequest request) {
		Long idEmpresa = (Long) request.getAttribute("empresaId");
		rol.setIdEmpresa(idEmpresa);
		return ResponseEntity.ok(roleService.save(rol));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	public ResponseEntity<Rol> update(@PathVariable Long id, @RequestBody Rol rol) {
		return roleService.update(id, rol)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	public ResponseEntity<Rol> disable(@PathVariable Long id) {
		return roleService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
