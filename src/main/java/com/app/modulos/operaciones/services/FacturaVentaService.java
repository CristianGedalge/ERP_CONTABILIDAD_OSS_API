package com.app.modulos.operaciones.services;

import com.app.modulos.contabilidad.entities.*;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import com.app.modulos.contabilidad.services.AsientoContableService;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.services.ConfiguracionService;
import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.TipoMovimientoInventario;
import com.app.modulos.inventario.repositories.ProductoRepository;
import com.app.modulos.inventario.services.MovimientoInventarioService;
import com.app.modulos.operaciones.entities.*;
import com.app.modulos.operaciones.repositories.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaVentaService {
    private final FacturaVentaRepository facturaRepository;
    private final CuentaPorCobrarRepository cpcRepository;
    private final ConfiguracionService configuracionService;
    private final MovimientoInventarioService movimientoService;
    private final AsientoContableService asientoService;
    private final CuentaContableRepository cuentaRepository;
    private final ProductoRepository productoRepository;

    public FacturaVentaService(
        FacturaVentaRepository facturaRepository,
        CuentaPorCobrarRepository cpcRepository,
        ConfiguracionService configuracionService,
        MovimientoInventarioService movimientoService,
        AsientoContableService asientoService,
        CuentaContableRepository cuentaRepository,
        ProductoRepository productoRepository
    ) {
        this.facturaRepository = facturaRepository;
        this.cpcRepository = cpcRepository;
        this.configuracionService = configuracionService;
        this.movimientoService = movimientoService;
        this.asientoService = asientoService;
        this.cuentaRepository = cuentaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<FacturaVenta> findAllByEmpresa(Long idEmpresa) {
        return facturaRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public FacturaVenta findById(Long id, Long idEmpresa) {
        FacturaVenta f = facturaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Factura de venta no encontrada"));
        if (!f.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: La factura no pertenece a tu empresa");
        }
        return f;
    }

    @Transactional
    public FacturaVenta registrarVenta(FacturaVenta factura, Long idEmpresa, Long idUsuario) {
        // 1. Obtener y validar Configuración del tenant
        Configuracion config = configuracionService.findByEmpresa(idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("Debe crear la configuración general de la empresa antes de facturar."));
        
        validarConfiguracionContable(config);

        if (factura.getFecha() == null) {
            factura.setFecha(LocalDate.now());
        }

        factura.setIdEmpresa(idEmpresa);
        factura.setEstado("EMITIDA");

        // Generar correlativo de factura
        if (factura.getNroFactura() == null || factura.getNroFactura().trim().isEmpty()) {
            factura.setNroFactura(generarNroFactura(idEmpresa));
        }

        // 2. Procesar detalles, calcular totales y reducir stock
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        BigDecimal costoTotalInventario = BigDecimal.ZERO;
        List<DetalleFacturaVenta> detalles = new ArrayList<>(factura.getDetalles());
        factura.getDetalles().clear();

        for (DetalleFacturaVenta det : detalles) {
            if (det.getProducto() == null || det.getProducto().getId() == null) {
                throw new IllegalArgumentException("Cada detalle debe especificar un producto válido.");
            }
            Producto prod = productoRepository.findByIdAndIdEmpresaAndEstadoTrue(
                det.getProducto().getId(), idEmpresa
            ).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: ID " + det.getProducto().getId()));

            BigDecimal cantidad = det.getCantidad() != null ? det.getCantidad() : BigDecimal.ZERO;
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }

            BigDecimal precio = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : prod.getPrecioVenta();
            det.setPrecioUnitario(precio);
            
            BigDecimal detSubtotal = cantidad.multiply(precio);
            det.setSubtotal(detSubtotal);
            subtotalAcumulado = subtotalAcumulado.add(detSubtotal);

            // Costo total de inventario (COGS)
            BigDecimal costoUnitario = prod.getCostoUnitario();
            costoTotalInventario = costoTotalInventario.add(cantidad.multiply(costoUnitario));

            // Agregar detalle
            factura.getDetalles().add(det);
            det.setFacturaVenta(factura);
            det.setProducto(prod);
        }

        factura.setSubtotal(subtotalAcumulado);
        BigDecimal desc = factura.getDescuento() != null ? factura.getDescuento() : BigDecimal.ZERO;
        factura.setDescuento(desc);
        factura.setTotal(subtotalAcumulado.subtract(desc));

        if (factura.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la factura debe ser mayor a cero.");
        }

        // Guardar la factura en la BD
        FacturaVenta guardada = facturaRepository.save(factura);

        // Reducir stock mediante Movimiento de Inventario después de guardar la factura
        for (DetalleFacturaVenta det : guardada.getDetalles()) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(det.getProducto());
            mov.setTipo(TipoMovimientoInventario.SALIDA);
            mov.setCantidad(det.getCantidad());
            mov.setFecha(LocalDateTime.now());
            mov.setDocumentoOrigen("FACTURA_VENTA");
            mov.setOrigenId(guardada.getId());
            movimientoService.registrarMovimiento(mov, idEmpresa);
        }

        // 3. Generar Asiento Contable Automático
        generarAsientoAutomatico(guardada, config, costoTotalInventario, idUsuario);

        // 4. Generar Cuenta por Cobrar si es a crédito
        if (Boolean.TRUE.equals(guardada.getEsCredito())) {
            CuentaPorCobrar cpc = new CuentaPorCobrar();
            cpc.setFacturaVenta(guardada);
            cpc.setMontoTotal(guardada.getTotal());
            cpc.setSaldo(guardada.getTotal());
            cpc.setFechaVencimiento(guardada.getFecha().plusDays(30)); // 30 días plazo estándar
            cpc.setEstado("PENDIENTE");
            cpc.setIdEmpresa(idEmpresa);
            cpcRepository.save(cpc);
        }

        return guardada;
    }

    @Transactional
    public void anularVenta(Long id, Long idEmpresa) {
        FacturaVenta f = findById(id, idEmpresa);
        if ("ANULADA".equals(f.getEstado())) {
            return;
        }

        f.setEstado("ANULADA");
        facturaRepository.save(f);

        // 1. Revertir stock (ingreso automático)
        for (DetalleFacturaVenta det : f.getDetalles()) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(det.getProducto());
            mov.setTipo(TipoMovimientoInventario.ENTRADA);
            mov.setCantidad(det.getCantidad());
            mov.setFecha(LocalDateTime.now());
            mov.setDocumentoOrigen("ANULACION_FACTURA_VENTA");
            mov.setOrigenId(f.getId());
            movimientoService.registrarMovimiento(mov, idEmpresa);
        }

        // 2. Anular Asiento Contable
        // Buscamos si existe un asiento registrado de esta factura
        List<AsientoContable> asientos = asientoService.findAllByEmpresa(idEmpresa);
        for (AsientoContable asiento : asientos) {
            if ("FACTURA_VENTA".equals(asiento.getOrigenDocumento()) && id.equals(asiento.getOrigenId())) {
                asientoService.anularAsiento(asiento.getId(), idEmpresa);
            }
        }

        // 3. Cancelar saldo de cartera si es crédito
        cpcRepository.findByFacturaVentaId(id).ifPresent(cpc -> {
            cpc.setSaldo(BigDecimal.ZERO);
            cpc.setEstado("ANULADA");
            cpcRepository.save(cpc);
        });
    }

    private void validarConfiguracionContable(Configuracion config) {
        if (config.getIdCuentaCaja() == null || config.getIdCuentaClientes() == null || 
            config.getIdCuentaVentas() == null || config.getIdCuentaIvaDebito() == null || 
            config.getIdCuentaItGasto() == null || config.getIdCuentaItPasivo() == null ||
            config.getIdCuentaInventario() == null || config.getIdCuentaCostoVentas() == null) {
            throw new IllegalArgumentException("Debe parametrizar todas las cuentas contables de automatización en la configuración general de la empresa antes de facturar.");
        }
    }

    private void generarAsientoAutomatico(FacturaVenta f, Configuracion config, BigDecimal costoInventario, Long idUsuario) {
        BigDecimal total = f.getTotal();
        BigDecimal tasaIva = config.getIva() != null ? config.getIva().divide(new BigDecimal("100")) : new BigDecimal("0.13");
        BigDecimal tasaIt = config.getIt() != null ? config.getIt().divide(new BigDecimal("100")) : new BigDecimal("0.03");

        BigDecimal ivaDebito = total.multiply(tasaIva);
        BigDecimal ingresoNeto = total.subtract(ivaDebito);
        BigDecimal itMonto = total.multiply(tasaIt);

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(f.getFecha());
        asiento.setGlosa("Registro de venta autom. - Factura Nro: " + f.getNroFactura());
        asiento.setEstado(EstadoAsiento.APROBADO);
        asiento.setOrigenDocumento("FACTURA_VENTA");
        asiento.setOrigenId(f.getId());

        List<DetalleAsiento> detalles = new ArrayList<>();

        // 1. DEBE: Caja/Bancos o Clientes (100% Total)
        DetalleAsiento detCobro = new DetalleAsiento();
        Long idCc = Boolean.TRUE.equals(f.getEsCredito()) ? config.getIdCuentaClientes() : config.getIdCuentaCaja();
        detCobro.setCuentaContable(obtenerCuentaReferencial(idCc));
        detCobro.setDebe(total);
        detCobro.setHaber(BigDecimal.ZERO);
        detCobro.setAsientoContable(asiento);
        detalles.add(detCobro);

        // 2. DEBE: IT Gasto (3%)
        DetalleAsiento detItG = new DetalleAsiento();
        detItG.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaItGasto()));
        detItG.setDebe(itMonto);
        detItG.setHaber(BigDecimal.ZERO);
        detItG.setAsientoContable(asiento);
        detalles.add(detItG);

        // 3. DEBE: Costo de Ventas (COGS)
        if (costoInventario.compareTo(BigDecimal.ZERO) > 0) {
            DetalleAsiento detCogs = new DetalleAsiento();
            detCogs.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaCostoVentas()));
            detCogs.setDebe(costoInventario);
            detCogs.setHaber(BigDecimal.ZERO);
            detCogs.setAsientoContable(asiento);
            detalles.add(detCogs);
        }

        // 4. HABER: Ventas / Ingresos (87%)
        DetalleAsiento detVentas = new DetalleAsiento();
        detVentas.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaVentas()));
        detVentas.setDebe(BigDecimal.ZERO);
        detVentas.setHaber(ingresoNeto);
        detVentas.setAsientoContable(asiento);
        detalles.add(detVentas);

        // 5. HABER: Débito Fiscal IVA (13%)
        DetalleAsiento detIva = new DetalleAsiento();
        detIva.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaIvaDebito()));
        detIva.setDebe(BigDecimal.ZERO);
        detIva.setHaber(ivaDebito);
        detIva.setAsientoContable(asiento);
        detalles.add(detIva);

        // 6. HABER: IT Pasivo por Pagar (3%)
        DetalleAsiento detItP = new DetalleAsiento();
        detItP.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaItPasivo()));
        detItP.setDebe(BigDecimal.ZERO);
        detItP.setHaber(itMonto);
        detItP.setAsientoContable(asiento);
        detalles.add(detItP);

        // 7. HABER: Inventarios (Costo)
        if (costoInventario.compareTo(BigDecimal.ZERO) > 0) {
            DetalleAsiento detInv = new DetalleAsiento();
            detInv.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaInventario()));
            detInv.setDebe(BigDecimal.ZERO);
            detInv.setHaber(costoInventario);
            detInv.setAsientoContable(asiento);
            detalles.add(detInv);
        }

        asiento.setDetalles(detalles);

        // Invocar el registro del Asiento Contable (aplica validación de partida doble y fecha de periodo)
        asientoService.registrarAsiento(asiento, f.getIdEmpresa(), idUsuario);
    }

    private CuentaContable obtenerCuentaReferencial(Long id) {
        CuentaContable ref = new CuentaContable();
        ref.setId(id);
        return ref;
    }

    private synchronized String generarNroFactura(Long idEmpresa) {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "FV-" + year + "-";
        String maxNro = facturaRepository.findMaxNroFacturaByPrefix(idEmpresa, prefix + "%");

        int next = 1;
        if (maxNro != null && !maxNro.trim().isEmpty() && maxNro.startsWith(prefix)) {
            try {
                String suffix = maxNro.substring(prefix.length());
                next = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException e) {
                // Falla del parseo mantiene en 1
            }
        }
        return String.format("%s%06d", prefix, next);
    }
}
