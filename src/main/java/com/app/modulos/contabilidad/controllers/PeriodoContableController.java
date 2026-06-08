package com.app.modulos.contabilidad.controllers;

import com.app.modulos.contabilidad.entities.PeriodoContable;
import com.app.modulos.contabilidad.services.PeriodoContableService;
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
@RequestMapping("/api/contabilidad/periodos")
@RequiresFeature("contabilidad")
public class PeriodoContableController {
    private final PeriodoContableService periodoService;

    public PeriodoContableController(PeriodoContableService periodoService) {
        this.periodoService = periodoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_READ')")
    public ResponseEntity<List<PeriodoContable>> list(
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
        return ResponseEntity.ok(periodoService.findAllByEmpresa(targetEmpresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    @Auditable(accion = "CREAR", modulo = "CONTABILIDAD")
    public ResponseEntity<?> registrar(
        @RequestBody PeriodoContable periodo,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId;
            if (isSuperAdmin) {
                targetEmpresaId = periodo.getIdEmpresa();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Como SUPERADMIN, debes especificar el idEmpresa.");
                }
            } else {
                targetEmpresaId = principal.getEmpresaId();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
                }
            }

            PeriodoContable registrado = periodoService.registrarPeriodo(periodo, targetEmpresaId);
            return ResponseEntity.ok(registrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar periodo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    @Auditable(accion = "EDITAR", modulo = "CONTABILIDAD")
    public ResponseEntity<?> cerrar(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            PeriodoContable cerrado = periodoService.cerrarPeriodo(id, targetEmpresaId);
            return ResponseEntity.ok(cerrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cerrar periodo: " + e.getMessage());
        }
    }
}
