package com.app.modulos.contabilidad.controllers;

import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.services.AsientoContableService;
import com.app.modulos.usuario.security.UserPrincipal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.modulos.config.RequiresFeature;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contabilidad/asientos")
@RequiresFeature("contabilidad")
public class AsientoContableController {
    private final AsientoContableService asientoService;

    public AsientoContableController(AsientoContableService asientoService) {
        this.asientoService = asientoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_READ')")
    public ResponseEntity<List<AsientoContable>> list(
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
        return ResponseEntity.ok(asientoService.findAllByEmpresa(targetEmpresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_READ')")
    public ResponseEntity<?> getById(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            AsientoContable asiento = asientoService.findById(id, targetEmpresaId);
            return ResponseEntity.ok(asiento);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener asiento contable: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> registrar(
        @RequestBody AsientoContable asiento,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId;
            if (isSuperAdmin) {
                targetEmpresaId = asiento.getIdEmpresa();
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
            AsientoContable registrado = asientoService.registrarAsiento(asiento, targetEmpresaId, idUsuario);
            return ResponseEntity.ok(registrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar asiento contable: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> actualizar(
        @PathVariable Long id,
        @RequestBody AsientoContable asiento,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            Long idUsuario = principal.getUsuario() != null ? principal.getUsuario().getId() : null;
            AsientoContable actualizado = asientoService.actualizarAsiento(id, asiento, targetEmpresaId, idUsuario);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar asiento contable: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
    public ResponseEntity<?> aprobar(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        try {
            boolean isSuperAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
            Long targetEmpresaId = isSuperAdmin ? null : principal.getEmpresaId();
            if (!isSuperAdmin && targetEmpresaId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: No se detectó empresa en tu sesión.");
            }

            AsientoContable aprobado = asientoService.aprobarAsiento(id, targetEmpresaId);
            return ResponseEntity.ok(aprobado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al aprobar asiento contable: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_CONTABILIDAD_WRITE')")
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

            AsientoContable anulado = asientoService.anularAsiento(id, targetEmpresaId);
            return ResponseEntity.ok(anulado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al anular asiento contable: " + e.getMessage());
        }
    }
}
