package com.app.modulos.reportes.services;

import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.entities.DetalleAsiento;
import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.operaciones.entities.CuentaPorCobrar;
import com.app.modulos.operaciones.entities.CuentaPorPagar;
import com.app.modulos.operaciones.entities.FacturaCompra;
import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.operaciones.entities.DetalleFacturaVenta;
import com.app.modulos.reportes.dtos.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportesService {

    @PersistenceContext
    private EntityManager entityManager;

    // REPORTES ANALÍTICOS

    public List<FacturaVenta> getVentasAnalitico(ReporteCriteriosDTO criterios, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacturaVenta> query = cb.createQuery(FacturaVenta.class);
        Root<FacturaVenta> root = query.from(FacturaVenta.class);
        
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));

        if (criterios.getFechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), criterios.getFechaDesde()));
        }
        if (criterios.getFechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), criterios.getFechaHasta()));
        }
        if (criterios.getClienteNombre() != null && !criterios.getClienteNombre().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("clienteNombre")), "%" + criterios.getClienteNombre().toLowerCase() + "%"));
        }
        if (criterios.getEstado() != null && !criterios.getEstado().equals("TODOS")) {
            predicates.add(cb.equal(root.get("estado"), criterios.getEstado()));
        }
        if (criterios.getProductoId() != null) {
            Join<Object, Object> detalles = root.join("detalles");
            predicates.add(cb.equal(detalles.get("producto").get("id"), criterios.getProductoId()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("fecha")), cb.desc(root.get("id")));

        return entityManager.createQuery(query).getResultList();
    }

    public List<FacturaCompra> getComprasAnalitico(ReporteCriteriosDTO criterios, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacturaCompra> query = cb.createQuery(FacturaCompra.class);
        Root<FacturaCompra> root = query.from(FacturaCompra.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));

        if (criterios.getFechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), criterios.getFechaDesde()));
        }
        if (criterios.getFechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), criterios.getFechaHasta()));
        }
        if (criterios.getProveedorNombre() != null && !criterios.getProveedorNombre().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("proveedorNombre")), "%" + criterios.getProveedorNombre().toLowerCase() + "%"));
        }
        if (criterios.getEstado() != null && !criterios.getEstado().equals("TODOS")) {
            predicates.add(cb.equal(root.get("estado"), criterios.getEstado()));
        }
        if (criterios.getProductoId() != null) {
            Join<Object, Object> detalles = root.join("detalles");
            predicates.add(cb.equal(detalles.get("producto").get("id"), criterios.getProductoId()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("fecha")), cb.desc(root.get("id")));

        return entityManager.createQuery(query).getResultList();
    }

    public List<Map<String, Object>> getKardex(Long productoId, LocalDate desde, LocalDate hasta, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MovimientoInventario> query = cb.createQuery(MovimientoInventario.class);
        Root<MovimientoInventario> root = query.from(MovimientoInventario.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));
        predicates.add(cb.equal(root.get("producto").get("id"), productoId));

        if (desde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha").as(LocalDate.class), desde));
        }
        if (hasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha").as(LocalDate.class), hasta));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("fecha")), cb.asc(root.get("id")));

        List<MovimientoInventario> movimientos = entityManager.createQuery(query).getResultList();

        // Calcular el saldo físico acumulado (rolling inventory balance)
        List<Map<String, Object>> res = new ArrayList<>();
        BigDecimal saldoAcumulado = BigDecimal.ZERO;

        for (MovimientoInventario mov : movimientos) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", mov.getId());
            map.put("fecha", mov.getFecha());
            map.put("tipo", mov.getTipo().name());
            map.put("cantidad", mov.getCantidad());
            map.put("documentoOrigen", mov.getDocumentoOrigen());
            map.put("origenId", mov.getOrigenId());

            if (mov.getTipo().name().equals("ENTRADA")) {
                saldoAcumulado = saldoAcumulado.add(mov.getCantidad());
            } else {
                saldoAcumulado = saldoAcumulado.subtract(mov.getCantidad());
            }
            map.put("saldoAcumulado", saldoAcumulado);
            res.add(map);
        }

        return res;
    }

    public List<?> getCarteraSaldos(String tipo, ReporteCriteriosDTO criterios, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        if ("COBRAR".equalsIgnoreCase(tipo)) {
            CriteriaQuery<CuentaPorCobrar> query = cb.createQuery(CuentaPorCobrar.class);
            Root<CuentaPorCobrar> root = query.from(CuentaPorCobrar.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));

            if (criterios.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), criterios.getFechaDesde()));
            }
            if (criterios.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaVencimiento"), criterios.getFechaHasta()));
            }
            if (criterios.getClienteNombre() != null && !criterios.getClienteNombre().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("facturaVenta").get("clienteNombre")), "%" + criterios.getClienteNombre().toLowerCase() + "%"));
            }
            if (criterios.getEstado() != null && !criterios.getEstado().equals("TODOS")) {
                predicates.add(cb.equal(root.get("estado"), criterios.getEstado()));
            }

            query.where(predicates.toArray(new Predicate[0]));
            query.orderBy(cb.asc(root.get("fechaVencimiento")));
            return entityManager.createQuery(query).getResultList();
        } else {
            CriteriaQuery<CuentaPorPagar> query = cb.createQuery(CuentaPorPagar.class);
            Root<CuentaPorPagar> root = query.from(CuentaPorPagar.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));

            if (criterios.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), criterios.getFechaDesde()));
            }
            if (criterios.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaVencimiento"), criterios.getFechaHasta()));
            }
            if (criterios.getProveedorNombre() != null && !criterios.getProveedorNombre().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("facturaCompra").get("proveedorNombre")), "%" + criterios.getProveedorNombre().toLowerCase() + "%"));
            }
            if (criterios.getEstado() != null && !criterios.getEstado().equals("TODOS")) {
                predicates.add(cb.equal(root.get("estado"), criterios.getEstado()));
            }

            query.where(predicates.toArray(new Predicate[0]));
            query.orderBy(cb.asc(root.get("fechaVencimiento")));
            return entityManager.createQuery(query).getResultList();
        }
    }

    public List<AsientoContable> getLibroDiario(ReporteCriteriosDTO criterios, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AsientoContable> query = cb.createQuery(AsientoContable.class);
        Root<AsientoContable> root = query.from(AsientoContable.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));

        if (criterios.getFechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), criterios.getFechaDesde()));
        }
        if (criterios.getFechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), criterios.getFechaHasta()));
        }
        if (criterios.getEstado() != null && !criterios.getEstado().equals("TODOS")) {
            predicates.add(cb.equal(root.get("estado").as(String.class), criterios.getEstado()));
        }
        if (criterios.getCuentaContableId() != null || criterios.getCentroCostoId() != null) {
            Join<AsientoContable, DetalleAsiento> detalles = root.join("detalles");
            if (criterios.getCuentaContableId() != null) {
                predicates.add(cb.equal(detalles.get("cuentaContable").get("id"), criterios.getCuentaContableId()));
            }
            if (criterios.getCentroCostoId() != null) {
                predicates.add(cb.equal(detalles.get("centroCosto").get("id"), criterios.getCentroCostoId()));
            }
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("fecha")), cb.asc(root.get("nroAsiento")));

        return entityManager.createQuery(query).getResultList();
    }

    // REPORTES GERENCIALES (KPIs y Gráficos)

    public ReporteGerencialDTO getVentasGerencial(LocalDate desde, LocalDate hasta, Long idEmpresa) {
        // 1. Obtener todas las ventas emitidas del periodo
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacturaVenta> query = cb.createQuery(FacturaVenta.class);
        Root<FacturaVenta> root = query.from(FacturaVenta.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));
        predicates.add(cb.equal(root.get("estado"), "EMITIDA"));

        if (desde != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        if (hasta != null) predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));

        query.where(predicates.toArray(new Predicate[0]));
        List<FacturaVenta> ventas = entityManager.createQuery(query).getResultList();

        // 2. Calcular KPIs
        BigDecimal totalFacturado = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalCosto = BigDecimal.ZERO;
        int conteo = ventas.size();

        for (FacturaVenta v : ventas) {
            totalFacturado = totalFacturado.add(v.getTotal());
            totalDescuentos = totalDescuentos.add(v.getDescuento());
            if (v.getDetalles() != null) {
                for (DetalleFacturaVenta det : v.getDetalles()) {
                    if (det.getProducto() != null) {
                        BigDecimal costoItem = det.getCantidad().multiply(det.getProducto().getCostoUnitario());
                        totalCosto = totalCosto.add(costoItem);
                    }
                }
            }
        }

        BigDecimal rentabilidadVal = BigDecimal.ZERO;
        if (totalFacturado.compareTo(BigDecimal.ZERO) > 0) {
            rentabilidadVal = totalFacturado.subtract(totalCosto)
                .multiply(new BigDecimal("100"))
                .divide(totalFacturado, 2, java.math.RoundingMode.HALF_UP);
        }

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalMonto", totalFacturado);
        kpis.put("descuentosTotal", totalDescuentos);
        kpis.put("rentabilidadEst", rentabilidadVal.setScale(2).toString() + "%");
        kpis.put("conteoRegistros", conteo);

        // 3. Generar tendencia (Ventas por Mes)
        Map<String, BigDecimal> tendenciaMap = new TreeMap<>();
        for (FacturaVenta v : ventas) {
            String mesKey = v.getFecha().getYear() + "-" + String.format("%02d", v.getFecha().getMonthValue());
            tendenciaMap.put(mesKey, tendenciaMap.getOrDefault(mesKey, BigDecimal.ZERO).add(v.getTotal()));
        }

        List<GerencialTrendDTO> tendenciaList = tendenciaMap.entrySet().stream()
                .map(e -> new GerencialTrendDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 4. Participación por Cliente
        Map<String, BigDecimal> participacionMap = new HashMap<>();
        for (FacturaVenta v : ventas) {
            participacionMap.put(v.getClienteNombre(), participacionMap.getOrDefault(v.getClienteNombre(), BigDecimal.ZERO).add(v.getTotal()));
        }

        List<GerencialShareDTO> shareList = participacionMap.entrySet().stream()
                .map(e -> new GerencialShareDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getValor().compareTo(a.getValor()))
                .limit(5)
                .collect(Collectors.toList());

        return new ReporteGerencialDTO(kpis, tendenciaList, shareList);
    }

    public ReporteGerencialDTO getComprasGerencial(LocalDate desde, LocalDate hasta, Long idEmpresa) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacturaCompra> query = cb.createQuery(FacturaCompra.class);
        Root<FacturaCompra> root = query.from(FacturaCompra.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("idEmpresa"), idEmpresa));
        predicates.add(cb.equal(root.get("estado"), "REGISTRADA"));

        if (desde != null) predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        if (hasta != null) predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));

        query.where(predicates.toArray(new Predicate[0]));
        List<FacturaCompra> compras = entityManager.createQuery(query).getResultList();

        BigDecimal totalComprado = BigDecimal.ZERO;
        BigDecimal totalDescuentos = BigDecimal.ZERO;
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        int conteo = compras.size();

        for (FacturaCompra c : compras) {
            totalComprado = totalComprado.add(c.getTotal());
            totalSubtotal = totalSubtotal.add(c.getSubtotal());
            totalDescuentos = totalDescuentos.add(c.getSubtotal().subtract(c.getTotal()));
        }

        BigDecimal ahorroDescuento = BigDecimal.ZERO;
        if (totalSubtotal.compareTo(BigDecimal.ZERO) > 0) {
            ahorroDescuento = totalDescuentos
                .multiply(new BigDecimal("100"))
                .divide(totalSubtotal, 2, java.math.RoundingMode.HALF_UP);
        }

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalMonto", totalComprado);
        kpis.put("descuentosTotal", totalDescuentos);
        kpis.put("rentabilidadEst", ahorroDescuento.setScale(2).toString() + "%");
        kpis.put("conteoRegistros", conteo);

        Map<String, BigDecimal> tendenciaMap = new TreeMap<>();
        for (FacturaCompra c : compras) {
            String mesKey = c.getFecha().getYear() + "-" + String.format("%02d", c.getFecha().getMonthValue());
            tendenciaMap.put(mesKey, tendenciaMap.getOrDefault(mesKey, BigDecimal.ZERO).add(c.getTotal()));
        }

        List<GerencialTrendDTO> tendenciaList = tendenciaMap.entrySet().stream()
                .map(e -> new GerencialTrendDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, BigDecimal> participacionMap = new HashMap<>();
        for (FacturaCompra c : compras) {
            participacionMap.put(c.getProveedorNombre(), participacionMap.getOrDefault(c.getProveedorNombre(), BigDecimal.ZERO).add(c.getTotal()));
        }

        List<GerencialShareDTO> shareList = participacionMap.entrySet().stream()
                .map(e -> new GerencialShareDTO(e.getKey(), e.getValue()))
                .sorted((a, b) -> b.getValor().compareTo(a.getValor()))
                .limit(5)
                .collect(Collectors.toList());

        return new ReporteGerencialDTO(kpis, tendenciaList, shareList);
    }

    // PDF GENERATION (LIBRO DIARIO OFICIAL DESDE BACKEND)

    public byte[] generarLibroDiarioPdf(ReporteCriteriosDTO criterios, Long idEmpresa) throws Exception {
        List<AsientoContable> asientos = getLibroDiario(criterios, idEmpresa);

        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Títulos y metadatos
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        Font tableBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        Font tableBodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.BLACK);

        Paragraph title = new Paragraph("LIBRO DIARIO CONTABLE OFICIAL", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph period = new Paragraph("Periodo: " + (criterios.getFechaDesde() != null ? criterios.getFechaDesde() : "Inicio") + 
                                         " al " + (criterios.getFechaHasta() != null ? criterios.getFechaHasta() : "Fin"), metaFont);
        period.setAlignment(Element.ALIGN_CENTER);
        document.add(period);
        document.add(new Paragraph("\n"));

        // Tabla libro diario
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 10, 45, 15, 15});

        // Headers
        String[] headers = {"Código / Nro", "Fecha", "Cuenta / Glosa", "Debe", "Haber"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
            cell.setBackgroundColor(new BaseColor(79, 70, 229)); // erp-primary
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        for (AsientoContable ast : asientos) {
            // Fila asiento principal
            PdfPCell cAst = new PdfPCell(new Paragraph(ast.getNroAsiento(), tableBodyBoldFont));
            cAst.setBackgroundColor(new BaseColor(240, 240, 240));
            table.addCell(cAst);

            PdfPCell cFecha = new PdfPCell(new Paragraph(ast.getFecha().toString(), tableBodyBoldFont));
            cFecha.setBackgroundColor(new BaseColor(240, 240, 240));
            table.addCell(cFecha);

            PdfPCell cGlosa = new PdfPCell(new Paragraph(ast.getGlosa(), tableBodyBoldFont));
            cGlosa.setBackgroundColor(new BaseColor(240, 240, 240));
            table.addCell(cGlosa);

            PdfPCell empty1 = new PdfPCell(new Paragraph(""));
            empty1.setBackgroundColor(new BaseColor(240, 240, 240));
            table.addCell(empty1);

            PdfPCell empty2 = new PdfPCell(new Paragraph(""));
            empty2.setBackgroundColor(new BaseColor(240, 240, 240));
            table.addCell(empty2);

            // Detalles
            for (DetalleAsiento det : ast.getDetalles()) {
                table.addCell(new Paragraph(det.getCuentaContable() != null ? det.getCuentaContable().getCodigo() : "", tableBodyFont));
                table.addCell(new Paragraph("", tableBodyFont));

                String ctaName = det.getCuentaContable() != null ? det.getCuentaContable().getNombre() : "";
                if (det.getHaber().compareTo(BigDecimal.ZERO) > 0) {
                    ctaName = "    " + ctaName; // Sangría para cuentas al haber
                }
                table.addCell(new Paragraph(ctaName, tableBodyFont));

                String debeVal = det.getDebe().compareTo(BigDecimal.ZERO) > 0 ? det.getDebe().setScale(2).toString() : "";
                PdfPCell cellDebe = new PdfPCell(new Paragraph(debeVal, tableBodyFont));
                cellDebe.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellDebe);

                String haberVal = det.getHaber().compareTo(BigDecimal.ZERO) > 0 ? det.getHaber().setScale(2).toString() : "";
                PdfPCell cellHaber = new PdfPCell(new Paragraph(haberVal, tableBodyFont));
                cellHaber.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellHaber);

                totalDebe = totalDebe.add(det.getDebe());
                totalHaber = totalHaber.add(det.getHaber());
            }
        }

        // Totales Finales
        PdfPCell labelCell = new PdfPCell(new Paragraph("SUMAS TOTALES", tableBodyBoldFont));
        labelCell.setColspan(3);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valDebeCell = new PdfPCell(new Paragraph(totalDebe.setScale(2).toString(), tableBodyBoldFont));
        valDebeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valDebeCell.setPadding(6);
        table.addCell(valDebeCell);

        PdfPCell valHaberCell = new PdfPCell(new Paragraph(totalHaber.setScale(2).toString(), tableBodyBoldFont));
        valHaberCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valHaberCell.setPadding(6);
        table.addCell(valHaberCell);

        document.add(table);

        document.close();
        return out.toByteArray();
    }

    // CONSTRUCTOR DINÁMICO QBE

    public List<Map<String, Object>> ejecutarQbe(ReporteQbeQueryDTO query, Long idEmpresa) {
        String entityName = "FacturaVenta";
        if ("COMPRAS".equalsIgnoreCase(query.getOrigen())) {
            entityName = "FacturaCompra";
        } else if ("INVENTARIO".equalsIgnoreCase(query.getOrigen())) {
            entityName = "MovimientoInventario";
        }

        // Armamos la consulta dinámica JPQL
        StringBuilder jpql = new StringBuilder("SELECT ");
        
        // Proyectar solo las columnas seleccionadas
        List<String> cols = query.getColumnas();
        for (int i = 0; i < cols.size(); i++) {
            jpql.append("e.").append(cols.get(i));
            if (i < cols.size() - 1) {
                jpql.append(", ");
            }
        }
        
        jpql.append(" FROM ").append(entityName).append(" e WHERE e.idEmpresa = :idEmpresa");

        // Añadir filtros dinámicos
        List<ReporteQbeFiltroDTO> filtros = query.getFiltros();
        Map<String, Object> params = new HashMap<>();
        params.put("idEmpresa", idEmpresa);

        if (filtros != null) {
            for (int i = 0; i < filtros.size(); i++) {
                ReporteQbeFiltroDTO f = filtros.get(i);
                String paramName = "param" + i;
                
                jpql.append(" AND ");
                
                if ("LIKE".equalsIgnoreCase(f.getOperador())) {
                    jpql.append("LOWER(e.").append(f.getCampo()).append(") LIKE :").append(paramName);
                    params.put(paramName, "%" + f.getValor().toLowerCase() + "%");
                } else if ("GREATER_THAN".equalsIgnoreCase(f.getOperador())) {
                    jpql.append("e.").append(f.getCampo()).append(" > :").append(paramName);
                    params.put(paramName, parseFilterValue(f.getValor(), f.getCampo(), entityName));
                } else if ("LESS_THAN".equalsIgnoreCase(f.getOperador())) {
                    jpql.append("e.").append(f.getCampo()).append(" < :").append(paramName);
                    params.put(paramName, parseFilterValue(f.getValor(), f.getCampo(), entityName));
                } else {
                    jpql.append("e.").append(f.getCampo()).append(" = :").append(paramName);
                    params.put(paramName, parseFilterValue(f.getValor(), f.getCampo(), entityName));
                }
            }
        }

        // Ordenamiento
        if (query.getOrdenarPor() != null && !query.getOrdenarPor().trim().isEmpty()) {
            jpql.append(" ORDER BY e.").append(query.getOrdenarPor());
            if ("DESC".equalsIgnoreCase(query.getDireccion())) {
                jpql.append(" DESC");
            }
        }

        jakarta.persistence.Query q = entityManager.createQuery(jpql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }

        List<?> rows = q.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            // Si solo se proyecta una columna, JPA retorna el objeto directo (no un array)
            if (cols.size() == 1) {
                map.put(cols.get(0), row);
            } else {
                Object[] arr = (Object[]) row;
                for (int i = 0; i < cols.size(); i++) {
                    map.put(cols.get(i), arr[i]);
                }
            }
            result.add(map);
        }

        return result;
    }

    private Object parseFilterValue(String val, String campo, String entity) {
        // Lógica de conversión de tipo para filtros dinámicos
        if ("total".equalsIgnoreCase(campo) || "subtotal".equalsIgnoreCase(campo) || "descuento".equalsIgnoreCase(campo) || "cantidad".equalsIgnoreCase(campo) || "precioUnitario".equalsIgnoreCase(campo)) {
            return new BigDecimal(val);
        }
        if ("fecha".equalsIgnoreCase(campo) || "fechaVencimiento".equalsIgnoreCase(campo)) {
            return LocalDate.parse(val);
        }
        if ("esCredito".equalsIgnoreCase(campo) || "estado".equalsIgnoreCase(campo)) {
            if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val)) {
                return Boolean.parseBoolean(val);
            }
        }
        return val;
    }
}
