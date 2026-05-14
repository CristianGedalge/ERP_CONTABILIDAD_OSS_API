package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.services.UserService;
import com.app.modulos.usuario.services.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;
	private final RoleService roleService;

	public UserController(UserService userService, RoleService roleService) {
		this.userService = userService;
		this.roleService = roleService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_READ')")
	public ResponseEntity<List<Usuario>> list(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			return ResponseEntity.ok(userService.findAll());
		}
		return ResponseEntity.ok(userService.findAllByEmpresa(principal.getEmpresaId()));
	}

	@GetMapping("/me")
	public ResponseEntity<Usuario> me(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(principal.getUsuario());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_READ')")
	public ResponseEntity<Usuario> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return userService.findById(id)
			.map(user -> {
				// Seguridad: Si no es SUPERADMIN, solo puede ver usuarios de SU empresa
				if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
					if (!user.getIdEmpresa().equals(principal.getEmpresaId())) {
						return ResponseEntity.status(HttpStatus.FORBIDDEN).<Usuario>build();
					}
				}
				return ResponseEntity.ok(user);
			})
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<?> create(@RequestBody Usuario usuario, @AuthenticationPrincipal UserPrincipal principal) {
		try {
			Long empresaId = principal.getEmpresaId();
			if (empresaId == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
			}
			
			usuario.setIdEmpresa(empresaId);
			
			// Si viene un rol, intentar cargarlo
			if (usuario.getRol() != null && usuario.getRol().getId() != null) {
				Optional<Rol> rolOpt = roleService.findById(usuario.getRol().getId());
				if (rolOpt.isPresent()) {
					usuario.setRol(rolOpt.get());
				}
			}
			
			Usuario guardado = userService.save(usuario);
			
			// Devolver respuesta simple para evitar bucles JSON
			java.util.Map<String, Object> response = new java.util.HashMap<>();
			response.put("mensaje", "Usuario registrado exitosamente");
			response.put("id", guardado.getId());
			response.put("username", guardado.getUsername());
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error al registrar: " + e.getMessage());
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<Usuario> update(
		@PathVariable Long id, 
		@RequestBody Usuario usuario,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		return userService.findById(id).map(existing -> {
			// Seguridad
			if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
				if (!existing.getIdEmpresa().equals(principal.getEmpresaId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).<Usuario>build();
				}
			}
			return userService.update(id, usuario)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<Usuario> disable(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return userService.findById(id).map(existing -> {
			// Seguridad
			if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
				if (!existing.getIdEmpresa().equals(principal.getEmpresaId())) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).<Usuario>build();
				}
			}
			return userService.disable(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}
}
