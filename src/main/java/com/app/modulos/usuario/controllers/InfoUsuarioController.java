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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.usuario.security.UserPrincipal;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/info-usuario")
public class InfoUsuarioController {
	private final InfoUsuarioService infoUsuarioService;

	public InfoUsuarioController(InfoUsuarioService infoUsuarioService) {
		this.infoUsuarioService = infoUsuarioService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_USER_READ')")
	public ResponseEntity<List<InfoUsuario>> list(@AuthenticationPrincipal UserPrincipal principal) {
		if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			return ResponseEntity.ok(infoUsuarioService.findAll());
		}
		return ResponseEntity.ok(infoUsuarioService.findAllByEmpresa(principal.getEmpresaId()));
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<InfoUsuario> getMe(@AuthenticationPrincipal UserPrincipal principal) {
		return infoUsuarioService.findByUsuarioId(principal.getUsuario().getId())
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<InfoUsuario> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
		return infoUsuarioService.findById(id)
			.map(info -> {
				// Seguridad: Solo el dueño, su ADMIN o SUPERADMIN pueden ver
				boolean isOwner = info.getUsuario() != null && info.getUsuario().getId().equals(principal.getUsuario().getId());
				boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
				boolean isAdminOfCompany = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) 
					&& info.getUsuario() != null && info.getUsuario().getIdEmpresa().equals(principal.getEmpresaId());

				if (isOwner || isSuperAdmin || isAdminOfCompany) {
					return ResponseEntity.ok(info);
				}
				return ResponseEntity.status(HttpStatus.FORBIDDEN).<InfoUsuario>build();
			})
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<InfoUsuario> create(
		@RequestBody InfoUsuario infoUsuario,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		// Seguridad: Forzamos que la información se asocie al usuario autenticado
		infoUsuario.setUsuario(principal.getUsuario());
		return ResponseEntity.ok(infoUsuarioService.save(infoUsuario));
	}

	@PutMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<InfoUsuario> updateMe(
		@RequestBody InfoUsuario infoUsuario,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		return infoUsuarioService.updateByUsuarioId(principal.getUsuario().getId(), infoUsuario)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	public ResponseEntity<InfoUsuario> update(
		@PathVariable Long id, 
		@RequestBody InfoUsuario infoUsuario,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		return infoUsuarioService.findById(id).map(existing -> {
			// Seguridad administrativa: Solo SUPERADMIN o ADMIN de la misma empresa
			boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
			boolean isAdminOfCompany = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) 
				&& existing.getUsuario() != null && existing.getUsuario().getIdEmpresa().equals(principal.getEmpresaId());

			if (isSuperAdmin || isAdminOfCompany) {
				return infoUsuarioService.update(id, infoUsuario)
					.map(ResponseEntity::ok)
					.orElseGet(() -> ResponseEntity.notFound().build());
			}
			return ResponseEntity.status(HttpStatus.FORBIDDEN).<InfoUsuario>build();
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<InfoUsuario> delete(@PathVariable Long id) {
		return infoUsuarioService.delete(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
