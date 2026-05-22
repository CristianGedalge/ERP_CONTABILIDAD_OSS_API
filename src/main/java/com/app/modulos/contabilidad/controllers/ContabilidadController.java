package com.app.modulos.contabilidad.controllers;

import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.repositories.AsientoContableRepository;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import com.app.modulos.contabilidad.services.ContabilidadComprasService;
import com.app.modulos.contabilidad.services.ContabilidadVentasService;
import com.app.modulos.usuario.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/contabilidad")
public class ContabilidadController {

	private final ContabilidadComprasService contabilidadComprasService;
	private final ContabilidadVentasService contabilidadVentasService;
	private final AsientoContableRepository asientoContableRepository;
	private final CuentaContableRepository cuentaContableRepository;

	public ContabilidadController(
		ContabilidadComprasService contabilidadComprasService,
		ContabilidadVentasService contabilidadVentasService,
		AsientoContableRepository asientoContableRepository,
		CuentaContableRepository cuentaContableRepository
	) {
		this.contabilidadComprasService = contabilidadComprasService;
		this.contabilidadVentasService = contabilidadVentasService;
		this.asientoContableRepository = asientoContableRepository;
		this.cuentaContableRepository = cuentaContableRepository;
	}

	@PostMapping("/sincronizar-compras")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	public ResponseEntity<?> sincronizarCompras(
		@RequestParam(value = "idEmpresa", required = false) Long idEmpresaParam,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Long idEmpresa = idEmpresaParam;
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (idEmpresa == null || !isSuperAdmin) {
			idEmpresa = principal.getEmpresaId();
		}
		if (idEmpresa == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "El ID de la empresa es requerido."));
		}
		try {
			int creados = contabilidadComprasService.sincronizarComprasOdoo(idEmpresa);
			return ResponseEntity.ok(Map.of(
				"mensaje", "Sincronización de compras completada con éxito",
				"asientosCreados", creados
			));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Error al sincronizar compras: " + e.getMessage()));
		}
	}

	@PostMapping("/sincronizar-ventas")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
	public ResponseEntity<?> sincronizarVentas(
		@RequestParam(value = "idEmpresa", required = false) Long idEmpresaParam,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		Long idEmpresa = idEmpresaParam;
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (idEmpresa == null || !isSuperAdmin) {
			idEmpresa = principal.getEmpresaId();
		}
		if (idEmpresa == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "El ID de la empresa es requerido."));
		}
		try {
			int creados = contabilidadVentasService.sincronizarVentasOdoo(idEmpresa);
			return ResponseEntity.ok(Map.of(
				"mensaje", "Sincronización de ventas completada con éxito",
				"asientosCreados", creados
			));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "Error al sincronizar ventas: " + e.getMessage()));
		}
	}

	@GetMapping("/asientos")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> obtenerAsientos(
		@RequestParam(value = "idEmpresa", required = false) Long idEmpresaParam,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (isSuperAdmin) {
			if (idEmpresaParam != null) {
				return ResponseEntity.ok(asientoContableRepository.findByIdEmpresaOrderByFechaDesc(idEmpresaParam));
			}
			return ResponseEntity.ok(asientoContableRepository.findAll());
		}
		Long idEmpresa = principal.getEmpresaId();
		if (idEmpresa == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "El usuario no pertenece a ninguna empresa."));
		}
		return ResponseEntity.ok(asientoContableRepository.findByIdEmpresaOrderByFechaDesc(idEmpresa));
	}

	@GetMapping("/cuentas")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> obtenerCuentas(
		@RequestParam(value = "idEmpresa", required = false) Long idEmpresaParam,
		@AuthenticationPrincipal UserPrincipal principal
	) {
		boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
		if (isSuperAdmin) {
			if (idEmpresaParam != null) {
				return ResponseEntity.ok(cuentaContableRepository.findByIdEmpresaAndEstadoTrueOrderByCodigoAsc(idEmpresaParam));
			}
			return ResponseEntity.ok(cuentaContableRepository.findAll());
		}
		Long idEmpresa = principal.getEmpresaId();
		if (idEmpresa == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "El usuario no pertenece a ninguna empresa."));
		}
		return ResponseEntity.ok(cuentaContableRepository.findByIdEmpresaAndEstadoTrueOrderByCodigoAsc(idEmpresa));
	}
}
