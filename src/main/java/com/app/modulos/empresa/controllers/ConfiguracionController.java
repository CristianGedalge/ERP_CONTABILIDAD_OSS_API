package com.app.modulos.empresa.controllers;

import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.services.ConfiguracionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.usuario.security.UserPrincipal;

import org.springframework.web.bind.annotation.RequestParam;

import com.app.modulos.config.RequiresFeature;

@RestController
@RequestMapping("/api/configuraciones")
@RequiresFeature("configuraciones")
public class ConfiguracionController {
	private final ConfiguracionService configuracionService;

	public ConfiguracionController(ConfiguracionService configuracionService) {
		this.configuracionService = configuracionService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONFIG_READ')")
	public ResponseEntity<Configuracion> get(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam(required = false) Long idEmpresa
	) {
		Long empresaId = principal.getEmpresaId();
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (isSuperAdmin && idEmpresa != null) {
			empresaId = idEmpresa;
		}
		return configuracionService.findByEmpresa(empresaId)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/all")
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<List<Configuracion>> listAllActivas() {
		return ResponseEntity.ok(configuracionService.findAllActivas());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONFIG_WRITE')")
	public ResponseEntity<Configuracion> create(
		@RequestBody Configuracion configuracion,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Long empresaId = principal.getEmpresaId();
		configuracion.setIdEmpresa(empresaId);
		return ResponseEntity.ok(configuracionService.save(configuracion));
	}

	@PutMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONFIG_WRITE')")
	public ResponseEntity<Configuracion> update(
		@AuthenticationPrincipal UserPrincipal principal, 
		@RequestBody Configuracion configuracion
	) {
		Long empresaId = principal.getEmpresaId();
		return configuracionService.updateByEmpresa(empresaId, configuracion)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
