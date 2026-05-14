package com.app.modulos.saas.controllers;

import com.app.modulos.saas.entities.Plan;
import com.app.modulos.saas.services.PlanService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planes")
public class PlanController {
	private final PlanService planService;

	public PlanController(PlanService planService) {
		this.planService = planService;
	}

	@GetMapping
	public ResponseEntity<List<Plan>> list(@AuthenticationPrincipal UserPrincipal principal) {
		// SUPERADMIN ve todos los planes
		if (principal != null && principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
			return ResponseEntity.ok(planService.findAll());
		}
		// Otros roles (ADMIN de empresa) solo ven planes habilitados
		return ResponseEntity.ok(planService.findActive());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Plan> get(@PathVariable Long id) {
		return planService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Plan> create(@RequestBody Plan plan) {
		return ResponseEntity.ok(planService.save(plan));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Plan> update(@PathVariable Long id, @RequestBody Plan plan) {
		return planService.update(id, plan)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Plan> toggleStatus(@PathVariable Long id) {
		return planService.toggleStatus(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SUPERADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		planService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
