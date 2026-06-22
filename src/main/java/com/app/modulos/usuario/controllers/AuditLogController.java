package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.AuditLog;
import com.app.modulos.usuario.security.UserPrincipal;
import com.app.modulos.usuario.services.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.modulos.config.RequiresFeature;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiresFeature("auditoria")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_AUDITORIA_READ')")
    public ResponseEntity<List<AuditLog>> getLogs(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        if (isSuperAdmin) {
            return ResponseEntity.ok(auditLogService.findAll());
        }

        Long empresaId = principal.getEmpresaId();
        if (empresaId == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(auditLogService.findByEmpresaId(empresaId));
    }
}
