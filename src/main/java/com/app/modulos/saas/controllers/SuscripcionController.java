package com.app.modulos.saas.controllers;

import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.services.SuscripcionService;
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
import com.app.modulos.config.Auditable;

@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {
	private final SuscripcionService suscripcionService;
	private final com.app.modulos.saas.services.PlanService planService;

	public SuscripcionController(SuscripcionService suscripcionService, com.app.modulos.saas.services.PlanService planService) {
		this.suscripcionService = suscripcionService;
		this.planService = planService;
	}

	@GetMapping
	public ResponseEntity<List<Suscripcion>> list(@AuthenticationPrincipal com.app.modulos.usuario.security.UserPrincipal principal) {
		if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			return ResponseEntity.ok(suscripcionService.findAll());
		}
		return ResponseEntity.ok(suscripcionService.findByEmpresa(principal.getEmpresaId()));
	}

	@GetMapping("/activa")
	public ResponseEntity<Suscripcion> getActive(@AuthenticationPrincipal com.app.modulos.usuario.security.UserPrincipal principal) {
		return suscripcionService.findActiveByEmpresa(principal.getEmpresaId())
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Suscripcion> get(@PathVariable Long id, @AuthenticationPrincipal com.app.modulos.usuario.security.UserPrincipal principal) {
		return suscripcionService.findById(id)
			.map(susc -> {
				// Seguridad: Solo SUPERADMIN o el ADMIN de la empresa propietaria
				boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
				if (isSuperAdmin || susc.getIdEmpresa().equals(principal.getEmpresaId())) {
					return ResponseEntity.ok(susc);
				}
				return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).<Suscripcion>build();
			})
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Auditable(accion = "CREAR", modulo = "SUSCRIPCION")
	public ResponseEntity<?> subscribe(
		@RequestBody Suscripcion body, 
		@AuthenticationPrincipal com.app.modulos.usuario.security.UserPrincipal principal
	) {
		// 1. Validar cuerpo básico
		if (body.getPlan() == null || body.getPlan().getId() == null) {
			return ResponseEntity.badRequest().body("El ID del plan es obligatorio");
		}

		// 2. Validar que el usuario tenga una empresa vinculada
		Long empresaId = principal.getEmpresaId();
		if (empresaId == null) {
			return ResponseEntity.status(400).body("Error: El usuario autenticado no tiene una empresa asociada en su perfil.");
		}

		// 3. Buscar el plan y crear la suscripción
		return planService.findById(body.getPlan().getId()).map(plan -> {
			try {
				Suscripcion nueva = new Suscripcion();
				nueva.setPlan(plan);
				nueva.setIdEmpresa(empresaId);
				nueva.setMontoPagado(plan.getPrecio());
				nueva.setTipoRenovacion(body.getTipoRenovacion() != null ? body.getTipoRenovacion() : "MENSUAL");
				
				Suscripcion guardada = suscripcionService.save(nueva);
				return ResponseEntity.ok(guardada);
			} catch (Exception e) {
				return ResponseEntity.status(500).body("Error al guardar la suscripción: " + e.getMessage());
			}
		}).orElseGet(() -> ResponseEntity.status(404).body("Error: El Plan con ID " + body.getPlan().getId() + " no existe."));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	@Auditable(accion = "ELIMINAR", modulo = "SUSCRIPCION")
	public ResponseEntity<Suscripcion> disable(@PathVariable Long id) {
		return suscripcionService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
