package com.app.modulos.saas.controllers;

import com.app.modulos.saas.entities.Configuracion;
import com.app.modulos.saas.services.ConfiguracionService;
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

@RestController
@RequestMapping("/api/configuraciones")
public class ConfiguracionController {
	private final ConfiguracionService configuracionService;

	public ConfiguracionController(ConfiguracionService configuracionService) {
		this.configuracionService = configuracionService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONFIG_READ')")
	public ResponseEntity<Configuracion> get(HttpServletRequest request) {
		Long empresaId = (Long) request.getAttribute("empresaId");
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
		HttpServletRequest request
	) {
		Long empresaId = (Long) request.getAttribute("empresaId");
		configuracion.setIdEmpresa(empresaId);
		return ResponseEntity.ok(configuracionService.save(configuracion));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONFIG_WRITE')")
	public ResponseEntity<Configuracion> update(@PathVariable Long id, @RequestBody Configuracion configuracion) {
		return configuracionService.update(id, configuracion)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
