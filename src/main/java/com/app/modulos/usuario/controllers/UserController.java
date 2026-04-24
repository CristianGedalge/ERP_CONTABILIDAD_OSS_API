package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_READ')")
	public ResponseEntity<List<Usuario>> list(HttpServletRequest request) {
		Long empresaId = (Long) request.getAttribute("empresaId");
		return ResponseEntity.ok(userService.findAllByEmpresa(empresaId));
	}

	@GetMapping("/me")
	public ResponseEntity<Usuario> me(HttpServletRequest request) {
		String correo = (String) request.getAttribute("correo");
		if (correo == null || correo.isBlank()) {
			return ResponseEntity.status(401).build();
		}
		return userService.findByCorreo(correo)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_READ')")
	public ResponseEntity<Usuario> get(@PathVariable Long id) {
		return userService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<Usuario> create(@RequestBody Usuario usuario, HttpServletRequest request) {
		Long empresaId = (Long) request.getAttribute("empresaId");
		usuario.setIdEmpresa(empresaId);
		return ResponseEntity.ok(userService.save(usuario));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody Usuario usuario) {
		return userService.update(id, usuario)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_WRITE')")
	public ResponseEntity<Usuario> disable(@PathVariable Long id) {
		return userService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
