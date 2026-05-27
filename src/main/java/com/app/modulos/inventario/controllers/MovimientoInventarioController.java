package com.app.modulos.inventario.controllers;

import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.services.MovimientoInventarioService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario/movimientos")
public class MovimientoInventarioController {
    private final MovimientoInventarioService movimientoService;

    public MovimientoInventarioController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_INVENTARIO_READ')")
    public ResponseEntity<List<MovimientoInventario>> list(
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
        return ResponseEntity.ok(movimientoService.findAllByEmpresa(targetEmpresaId));
    }

    @GetMapping("/producto/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_INVENTARIO_READ')")
    public ResponseEntity<List<MovimientoInventario>> getByProducto(
        @PathVariable Long id,
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
        return ResponseEntity.ok(movimientoService.findByProducto(id, targetEmpresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_INVENTARIO_WRITE')")
    public ResponseEntity<?> registrar(
        @RequestBody MovimientoInventario movimiento,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId;
            if (isSuperAdmin) {
                targetEmpresaId = movimiento.getIdEmpresa();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Como SUPERADMIN, debes especificar el idEmpresa.");
                }
            } else {
                targetEmpresaId = principal.getEmpresaId();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
                }
            }

            MovimientoInventario registrado = movimientoService.registrarMovimiento(movimiento, targetEmpresaId);
            return ResponseEntity.ok(registrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar movimiento: " + e.getMessage());
        }
    }
}
