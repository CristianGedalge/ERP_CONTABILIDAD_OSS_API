package com.app.modulos.odoo.controllers;

import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.services.ConfiguracionService;
import com.app.modulos.odoo.services.OdooClientService;
import com.app.modulos.usuario.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/odoo", "/api/configuraciones/odoo"})
public class OdooController {

	private final OdooClientService odooClientService;
	private final ConfiguracionService configuracionService;

	public OdooController(OdooClientService odooClientService, ConfiguracionService configuracionService) {
		this.odooClientService = odooClientService;
		this.configuracionService = configuracionService;
	}

	@GetMapping("/productos")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_PRODUCTO_READ') or hasAuthority('PERM_CONFIG_READ')")
	public ResponseEntity<List<Map<String, Object>>> getOdooProductos(@AuthenticationPrincipal UserPrincipal principal) {
		Long empresaId = principal.getEmpresaId();
		Configuracion config = configuracionService.findByEmpresa(empresaId)
			.orElseThrow(() -> new RuntimeException("No se encontró la configuración para esta empresa."));
		try {
			List<Map<String, Object>> productos = odooClientService.obtenerProductosPorCompania(config);
			return ResponseEntity.ok(productos);
		} catch (Exception e) {
			throw new RuntimeException("Error consultando productos de Odoo: " + e.getMessage(), e);
		}
	}

	@GetMapping("/ventas")
	@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_VENTA_READ') or hasAuthority('PERM_CONFIG_READ')")
	public ResponseEntity<List<Map<String, Object>>> getOdooVentas(@AuthenticationPrincipal UserPrincipal principal) {
		Long empresaId = principal.getEmpresaId();
		Configuracion config = configuracionService.findByEmpresa(empresaId)
			.orElseThrow(() -> new RuntimeException("No se encontró la configuración para esta empresa."));
		try {
			List<Map<String, Object>> ventas = odooClientService.obtenerOrdenesVentaPorCompania(config);
			return ResponseEntity.ok(ventas);
		} catch (Exception e) {
			throw new RuntimeException("Error consultando órdenes de venta de Odoo: " + e.getMessage(), e);
		}
	}
}
