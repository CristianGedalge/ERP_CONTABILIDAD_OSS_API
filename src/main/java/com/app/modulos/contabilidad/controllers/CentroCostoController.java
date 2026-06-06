package com.app.modulos.contabilidad.controllers;

import com.app.modulos.contabilidad.entities.CentroCosto;
import com.app.modulos.contabilidad.services.CentroCostoService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.config.RequiresFeature;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contabilidad/centros-costo")
@RequiresFeature("contabilidad")
public class CentroCostoController {
    private final CentroCostoService centroService;

    public CentroCostoController(CentroCostoService centroService) {
        this.centroService = centroService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_READ')")
    public ResponseEntity<List<CentroCosto>> list(
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
        return ResponseEntity.ok(centroService.findAllByEmpresa(targetEmpresaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> crear(
        @RequestBody CentroCosto cc,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId;
            if (isSuperAdmin) {
                targetEmpresaId = cc.getIdEmpresa();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Como SUPERADMIN, debes especificar el idEmpresa.");
                }
            } else {
                targetEmpresaId = principal.getEmpresaId();
                if (targetEmpresaId == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
                }
            }

            CentroCosto creado = centroService.crear(cc, targetEmpresaId);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear centro de costo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> actualizar(
        @PathVariable Long id,
        @RequestBody CentroCosto cc,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            CentroCosto actualizado = centroService.actualizar(id, cc, targetEmpresaId);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar centro de costo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> eliminar(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            centroService.eliminar(id, targetEmpresaId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar centro de costo: " + e.getMessage());
        }
    }
}
