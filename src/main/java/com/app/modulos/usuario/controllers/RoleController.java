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

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.usuario.security.UserPrincipal;
import org.springframework.http.HttpStatus;

import com.app.modulos.config.RequiresFeature;
import com.app.modulos.config.Auditable;

@RestController
@RequestMapping("/api/roles")
@RequiresFeature("roles-permisos")
public class RoleController {
	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_READ')")
	public ResponseEntity<List<Rol>> list(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			return ResponseEntity.ok(roleService.findAll());
		}
		return ResponseEntity.ok(roleService.findAllByEmpresa(principal.getEmpresaId()));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_READ')")
	public ResponseEntity<Rol> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return roleService.findById(id)
			.map(rol -> {
				// Seguridad: Si no es SUPERADMIN, solo puede ver roles de SU empresa
				if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
					if (!rol.getIdEmpresa().equals(principal.getEmpresaId())) {
						return ResponseEntity.status(HttpStatus.FORBIDDEN).<Rol>build();
					}
				}
				return ResponseEntity.ok(rol);
			})
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	@Auditable(accion = "CREAR", modulo = "ROLES_PERMISOS")
	public ResponseEntity<Rol> create(@RequestBody Rol rol, @AuthenticationPrincipal UserPrincipal principal) {
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (!isSuperAdmin) {
			rol.setIdEmpresa(principal.getEmpresaId());
		}
		return ResponseEntity.ok(roleService.save(rol));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	@Auditable(accion = "EDITAR", modulo = "ROLES_PERMISOS")
	public ResponseEntity<Rol> update(
		@PathVariable Long id, 
		@RequestBody Rol rol,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		return roleService.findById(id).map(existing -> {
			boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
			// Seguridad
			if (!isSuperAdmin) {
				if (!existing.getIdEmpresa().equals(principal.getEmpresaId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).<Rol>build();
				}
				rol.setIdEmpresa(principal.getEmpresaId()); // Prevenir cambio de empresa por un ADMIN
			}
			return roleService.update(id, rol)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_ROL_WRITE')")
	@Auditable(accion = "ELIMINAR", modulo = "ROLES_PERMISOS")
	public ResponseEntity<Rol> disable(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return roleService.findById(id).map(existing -> {
			// Seguridad
			if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
				if (!existing.getIdEmpresa().equals(principal.getEmpresaId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).<Rol>build();
				}
			}
			return roleService.disable(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}
}
