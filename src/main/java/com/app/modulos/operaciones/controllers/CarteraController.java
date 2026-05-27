package com.app.modulos.operaciones.controllers;

import com.app.modulos.operaciones.entities.CuentaPorCobrar;
import com.app.modulos.operaciones.entities.CuentaPorPagar;
import com.app.modulos.operaciones.services.CarteraService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operaciones")
public class CarteraController {
    private final CarteraService carteraService;

    public CarteraController(CarteraService carteraService) {
        this.carteraService = carteraService;
    }

    @GetMapping("/cuentas-cobrar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_READ')")
    public ResponseEntity<List<CuentaPorCobrar>> listCuentasPorCobrar(
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
        return ResponseEntity.ok(carteraService.getCuentasPorCobrar(targetEmpresaId));
    }

    @GetMapping("/cuentas-pagar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_READ')")
    public ResponseEntity<List<CuentaPorPagar>> listCuentasPorPagar(
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
        return ResponseEntity.ok(carteraService.getCuentasPorPagar(targetEmpresaId));
    }

    @PostMapping("/cuentas-cobrar/{id}/pagar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_WRITE')")
    public ResponseEntity<?> registrarCobro(
        @PathVariable Long id,
        @RequestBody Map<String, BigDecimal> body,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            BigDecimal monto = body != null ? body.get("monto") : null;
            if (monto == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Debe especificar el 'monto' en el cuerpo de la solicitud.");
            }

            Long idUsuario = principal.getUsuario() != null ? principal.getUsuario().getId() : null;
            CuentaPorCobrar cpc = carteraService.registrarCobro(id, monto, targetEmpresaId, idUsuario);
            return ResponseEntity.ok(cpc);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el cobro: " + e.getMessage());
        }
    }

    @PostMapping("/cuentas-pagar/{id}/pagar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_WRITE')")
    public ResponseEntity<?> registrarPago(
        @PathVariable Long id,
        @RequestBody Map<String, BigDecimal> body,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            BigDecimal monto = body != null ? body.get("monto") : null;
            if (monto == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Debe especificar el 'monto' en el cuerpo de la solicitud.");
            }

            Long idUsuario = principal.getUsuario() != null ? principal.getUsuario().getId() : null;
            CuentaPorPagar cpp = carteraService.registrarPago(id, monto, targetEmpresaId, idUsuario);
            return ResponseEntity.ok(cpp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el pago: " + e.getMessage());
        }
    }
}
