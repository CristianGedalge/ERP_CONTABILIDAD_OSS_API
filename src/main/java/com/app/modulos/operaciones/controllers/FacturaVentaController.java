package com.app.modulos.operaciones.controllers;

import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.operaciones.services.FacturaVentaService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.config.RequiresFeature;
import com.app.modulos.config.Auditable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operaciones/ventas")
@RequiresFeature("ventas")
public class FacturaVentaController {
    private final FacturaVentaService ventaService;

    public FacturaVentaController(FacturaVentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_READ')")
    public ResponseEntity<List<FacturaVenta>> list(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long idEmpresa
    ) {
        boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
        Long targetEmpresaId = idEmpresa;
        if (!isSuperAdmin || targetEmpresaId == null) {
            targetEmpresaId = principal.getEmpresaId();
        }
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ventaService.findAllByEmpresa(targetEmpresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_WRITE')")
    @Auditable(accion = "CREAR", modulo = "VENTAS")
    public ResponseEntity<?> registrar(
        @RequestBody FacturaVenta factura,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId;
            if (isSuperAdmin) {
                targetEmpresaId = factura.getIdEmpresa();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Como SUPERADMIN, debes especificar el idEmpresa.");
                }
            } else {
                targetEmpresaId = principal.getEmpresaId();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
                }
            }

            Long idUsuario = principal.getUsuario() != null ? principal.getUsuario().getId() : null;
            FacturaVenta registrada = ventaService.registrarVenta(factura, targetEmpresaId, idUsuario);
            return ResponseEntity.ok(registrada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar factura de venta: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_WRITE')")
    @Auditable(accion = "ANULAR", modulo = "VENTAS")
    public ResponseEntity<?> anular(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            ventaService.anularVenta(id, targetEmpresaId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al anular factura de venta: " + e.getMessage());
        }
    }
}
