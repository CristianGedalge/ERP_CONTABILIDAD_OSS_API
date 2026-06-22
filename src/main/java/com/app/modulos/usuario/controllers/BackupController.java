package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.entities.BackupMetadata;
import com.app.modulos.usuario.security.UserPrincipal;
import com.app.modulos.usuario.services.BackupService;
import com.app.modulos.usuario.services.BackupExportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.app.modulos.usuario.services.BackupImportService;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/backups")
public class BackupController {
    private final BackupService backupService;
    private final BackupExportService backupExportService;
    private final BackupImportService backupImportService;

    public BackupController(BackupService backupService, BackupExportService backupExportService, BackupImportService backupImportService) {
        this.backupService = backupService;
        this.backupExportService = backupExportService;
        this.backupImportService = backupImportService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_BACKUP_READ')")
    public ResponseEntity<List<BackupMetadata>> getBackups(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        if (isSuperAdmin) {
            return ResponseEntity.ok(backupService.findAll());
        }

        Long empresaId = principal.getEmpresaId();
        if (empresaId == null) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(backupService.findByEmpresaId(empresaId));
    }

    @PostMapping("/exportar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_BACKUP_WRITE')")
    public ResponseEntity<BackupMetadata> exportar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long idEmpresa) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        Long targetEmpresaId;
        if (isSuperAdmin) {
            if (idEmpresa == null) {
                return ResponseEntity.badRequest().build();
            }
            targetEmpresaId = idEmpresa;
        } else {
            targetEmpresaId = principal.getEmpresaId();
            if (targetEmpresaId == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        BackupMetadata backup = backupExportService.exportarEmpresa(targetEmpresaId, principal.getUsername());
        return ResponseEntity.ok(backup);
    }

    @PostMapping("/exportar-global")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<BackupMetadata> exportarGlobal(@AuthenticationPrincipal UserPrincipal principal) {
        BackupMetadata backup = backupExportService.exportarGlobal(principal.getUsername());
        return ResponseEntity.ok(backup);
    }

    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_BACKUP_READ')")
    public ResponseEntity<Resource> descargar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        
        BackupMetadata backup = backupService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Backup no encontrado con ID: " + id));

        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        if (!isSuperAdmin) {
            Long userEmpresaId = principal.getEmpresaId();
            if (userEmpresaId == null || !userEmpresaId.equals(backup.getIdEmpresa())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        try {
            Path filePath = Paths.get(backup.getStorageUrl());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.getNombreArchivo() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/importar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_BACKUP_WRITE')")
    public ResponseEntity<?> importar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long idEmpresa) {
        
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        Long targetEmpresaId;
        if (isSuperAdmin) {
            if (idEmpresa == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "El ID de la empresa es requerido para SuperAdministradores."));
            }
            targetEmpresaId = idEmpresa;
        } else {
            targetEmpresaId = principal.getEmpresaId();
            if (targetEmpresaId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "El usuario no tiene una empresa asociada."));
            }
        }

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", "El archivo está vacío."));
            }
            backupImportService.importarEmpresa(targetEmpresaId, file.getBytes(), principal.getUsername());
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Datos restaurados con éxito para la empresa."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al restaurar los datos: " + e.getMessage()));
        }
    }
}
