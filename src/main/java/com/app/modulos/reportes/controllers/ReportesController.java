package com.app.modulos.reportes.controllers;

import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.operaciones.entities.FacturaCompra;
import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.reportes.dtos.ReporteCriteriosDTO;
import com.app.modulos.reportes.dtos.ReporteGerencialDTO;
import com.app.modulos.reportes.dtos.ReporteQbeQueryDTO;
import com.app.modulos.reportes.services.ReportesService;
import com.app.modulos.usuario.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.app.modulos.config.RequiresFeature;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasAuthority('PERM_OPERACIONES_READ') or hasAuthority('PERM_CONTABILIDAD_READ')")
@RequiresFeature("reportes")
public class ReportesController {

    private final ReportesService reportesService;

    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    // 1. Reporte Analítico de Ventas
    @PostMapping("/ventas/analitico")
    public ResponseEntity<List<FacturaVenta>> getVentasAnalitico(
            @RequestBody ReporteCriteriosDTO criterios,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getVentasAnalitico(criterios, targetEmpresaId));
    }

    // 2. Reporte Gerencial de Ventas
    @GetMapping("/ventas/gerencial")
    public ResponseEntity<ReporteGerencialDTO> getVentasGerencial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getVentasGerencial(fechaDesde, fechaHasta, targetEmpresaId));
    }

    // 3. Reporte Analítico de Compras
    @PostMapping("/compras/analitico")
    public ResponseEntity<List<FacturaCompra>> getComprasAnalitico(
            @RequestBody ReporteCriteriosDTO criterios,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getComprasAnalitico(criterios, targetEmpresaId));
    }

    // 4. Reporte Gerencial de Compras
    @GetMapping("/compras/gerencial")
    public ResponseEntity<ReporteGerencialDTO> getComprasGerencial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getComprasGerencial(fechaDesde, fechaHasta, targetEmpresaId));
    }

    // 5. Kardex Físico (Inventario)
    @GetMapping("/inventario/kardex/{productoId}")
    public ResponseEntity<List<Map<String, Object>>> getKardex(
            @PathVariable Long productoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getKardex(productoId, fechaDesde, fechaHasta, targetEmpresaId));
    }

    // 6. Saldos de Cartera (Cuentas por cobrar / pagar)
    @PostMapping("/cartera/saldos")
    public ResponseEntity<List<?>> getCarteraSaldos(
            @RequestParam String tipo,
            @RequestBody ReporteCriteriosDTO criterios,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getCarteraSaldos(tipo, criterios, targetEmpresaId));
    }

    // 7. Libro Diario (Contabilidad)
    @PostMapping("/contabilidad/libro-diario")
    public ResponseEntity<List<AsientoContable>> getLibroDiario(
            @RequestBody ReporteCriteriosDTO criterios,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.getLibroDiario(criterios, targetEmpresaId));
    }

    // 8. PDF Oficial del Libro Diario (Contabilidad)
    @PostMapping("/contabilidad/libro-diario/pdf")
    public ResponseEntity<byte[]> descargarLibroDiarioPdf(
            @RequestBody ReporteCriteriosDTO criterios,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] pdfBytes = reportesService.generarLibroDiarioPdf(criterios, targetEmpresaId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Libro_Diario.pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 9. Consulta Dinámica QBE
    @PostMapping("/qbe")
    public ResponseEntity<List<Map<String, Object>>> ejecutarQbe(
            @RequestBody ReporteQbeQueryDTO query,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long targetEmpresaId = getTargetEmpresaId(principal, null);
        if (targetEmpresaId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportesService.ejecutarQbe(query, targetEmpresaId));
    }

    // Helper para obtener el Tenant ID de forma segura
    private Long getTargetEmpresaId(UserPrincipal principal, Long requestedEmpresaId) {
        boolean isSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
        if (isSuperAdmin && requestedEmpresaId != null) {
            return requestedEmpresaId;
        }
        return principal.getEmpresaId();
    }
}
