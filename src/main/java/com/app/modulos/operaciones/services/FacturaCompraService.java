package com.app.modulos.operaciones.services;

import com.app.modulos.contabilidad.entities.*;
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
public class FacturaCompraService {
    private final FacturaCompraRepository facturaRepository;
    private final CuentaPorPagarRepository cppRepository;
    private final ConfiguracionService configuracionService;
    private final MovimientoInventarioService movimientoService;
    private final AsientoContableService asientoService;
    private final ProductoRepository productoRepository;

    public FacturaCompraService(
        FacturaCompraRepository facturaRepository,
        CuentaPorPagarRepository cppRepository,
        ConfiguracionService configuracionService,
        MovimientoInventarioService movimientoService,
        AsientoContableService asientoService,
        ProductoRepository productoRepository
    ) {
        this.facturaRepository = facturaRepository;
        this.cppRepository = cppRepository;
        this.configuracionService = configuracionService;
        this.movimientoService = movimientoService;
        this.asientoService = asientoService;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<FacturaCompra> findAllByEmpresa(Long idEmpresa) {
        return facturaRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public FacturaCompra findById(Long id, Long idEmpresa) {
        FacturaCompra f = facturaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Factura de compra no encontrada"));
        if (!f.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: La factura no pertenece a tu empresa");
        }
        return f;
    }

    @Transactional
    public FacturaCompra registrarCompra(FacturaCompra factura, Long idEmpresa, Long idUsuario) {
        // 1. Obtener y validar Configuración del tenant
        Configuracion config = configuracionService.findByEmpresa(idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("Debe crear la configuración general de la empresa antes de registrar compras."));
        
        validarConfiguracionContable(config);

        if (factura.getFecha() == null) {
            factura.setFecha(LocalDate.now());
        }

        factura.setIdEmpresa(idEmpresa);
        factura.setEstado("REGISTRADA");

        // 2. Procesar detalles, calcular totales e ingresar stock
        BigDecimal subtotalAcumulado = BigDecimal.ZERO;
        List<DetalleFacturaCompra> detalles = new ArrayList<>(factura.getDetalles());
        factura.getDetalles().clear();

        for (DetalleFacturaCompra det : detalles) {
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

            BigDecimal costo = det.getCostoUnitario() != null ? det.getCostoUnitario() : prod.getCostoUnitario();
            det.setCostoUnitario(costo);

            BigDecimal detSubtotal = cantidad.multiply(costo);
            det.setSubtotal(detSubtotal);
            subtotalAcumulado = subtotalAcumulado.add(detSubtotal);

            // Agregar detalle
            factura.getDetalles().add(det);
            det.setFacturaCompra(factura);
            det.setProducto(prod);
        }

        factura.setSubtotal(subtotalAcumulado);
        factura.setTotal(subtotalAcumulado);

        if (factura.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la factura debe ser mayor a cero.");
        }

        // Guardar la factura
        FacturaCompra guardada = facturaRepository.save(factura);

        // Ingresar stock mediante Movimiento de Inventario y actualizar costo del producto
        for (DetalleFacturaCompra det : guardada.getDetalles()) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(det.getProducto());
            mov.setTipo(TipoMovimientoInventario.ENTRADA);
            mov.setCantidad(det.getCantidad());
            mov.setCostoUnitario(det.getCostoUnitario());
            mov.setFecha(LocalDateTime.now());
            mov.setDocumentoOrigen("FACTURA_COMPRA");
            mov.setOrigenId(guardada.getId());
            movimientoService.registrarMovimiento(mov, idEmpresa);
        }

        // 3. Generar Asiento Contable Automático
        generarAsientoAutomatico(guardada, config, idUsuario);

        // 4. Generar Cuenta por Pagar si es a crédito
        if (Boolean.TRUE.equals(guardada.getEsCredito())) {
            CuentaPorPagar cpp = new CuentaPorPagar();
            cpp.setFacturaCompra(guardada);
            cpp.setMontoTotal(guardada.getTotal());
            cpp.setSaldo(guardada.getTotal());
            cpp.setFechaVencimiento(guardada.getFecha().plusDays(30)); // 30 días plazo estándar
            cpp.setEstado("PENDIENTE");
            cpp.setIdEmpresa(idEmpresa);
            cppRepository.save(cpp);
        }

        return guardada;
    }

    private void validarConfiguracionContable(Configuracion config) {
        if (config.getIdCuentaCaja() == null || config.getIdCuentaProveedores() == null || 
            config.getIdCuentaIvaCredito() == null || config.getIdCuentaInventario() == null) {
            throw new IllegalArgumentException("Debe parametrizar las cuentas de Caja, Proveedores, Crédito Fiscal IVA e Inventario en la configuración general antes de registrar compras.");
        }
    }

    private void generarAsientoAutomatico(FacturaCompra f, Configuracion config, Long idUsuario) {
        BigDecimal total = f.getTotal();
        BigDecimal tasaIva = config.getIva() != null ? config.getIva().divide(new BigDecimal("100")) : new BigDecimal("0.13");

        BigDecimal ivaCredito = total.multiply(tasaIva);
        BigDecimal costoNeto = total.subtract(ivaCredito);

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(f.getFecha());
        asiento.setGlosa("Registro de compra autom. - Factura Nro: " + f.getNroFactura());
        asiento.setEstado(EstadoAsiento.APROBADO);
        asiento.setOrigenDocumento("FACTURA_COMPRA");
        asiento.setOrigenId(f.getId());

        List<DetalleAsiento> detalles = new ArrayList<>();

        // 1. DEBE: Inventarios (87% Costo Neto)
        DetalleAsiento detInv = new DetalleAsiento();
        detInv.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaInventario()));
        detInv.setDebe(costoNeto);
        detInv.setHaber(BigDecimal.ZERO);
        detInv.setAsientoContable(asiento);
        detalles.add(detInv);

        // 2. DEBE: Crédito Fiscal IVA (13%)
        DetalleAsiento detIva = new DetalleAsiento();
        detIva.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaIvaCredito()));
        detIva.setDebe(ivaCredito);
        detIva.setHaber(BigDecimal.ZERO);
        detIva.setAsientoContable(asiento);
        detalles.add(detIva);

        // 3. HABER: Caja/Bancos o Proveedores (100% Total)
        DetalleAsiento detPago = new DetalleAsiento();
        Long idCc = Boolean.TRUE.equals(f.getEsCredito()) ? config.getIdCuentaProveedores() : config.getIdCuentaCaja();
        detPago.setCuentaContable(obtenerCuentaReferencial(idCc));
        detPago.setDebe(BigDecimal.ZERO);
        detPago.setHaber(total);
        detPago.setAsientoContable(asiento);
        detalles.add(detPago);

        asiento.setDetalles(detalles);

        // Registrar Asiento
        asientoService.registrarAsiento(asiento, f.getIdEmpresa(), idUsuario);
    }

    private CuentaContable obtenerCuentaReferencial(Long id) {
        CuentaContable ref = new CuentaContable();
        ref.setId(id);
        return ref;
    }
}
