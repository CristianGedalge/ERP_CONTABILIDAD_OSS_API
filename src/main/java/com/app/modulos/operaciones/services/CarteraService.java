package com.app.modulos.operaciones.services;

import com.app.modulos.contabilidad.entities.*;
import com.app.modulos.contabilidad.services.AsientoContableService;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.services.ConfiguracionService;
import com.app.modulos.operaciones.entities.*;
import com.app.modulos.operaciones.repositories.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarteraService {
    private final CuentaPorCobrarRepository cpcRepository;
    private final CuentaPorPagarRepository cppRepository;
    private final ConfiguracionService configuracionService;
    private final AsientoContableService asientoService;

    public CarteraService(
        CuentaPorCobrarRepository cpcRepository,
        CuentaPorPagarRepository cppRepository,
        ConfiguracionService configuracionService,
        AsientoContableService asientoService
    ) {
        this.cpcRepository = cpcRepository;
        this.cppRepository = cppRepository;
        this.configuracionService = configuracionService;
        this.asientoService = asientoService;
    }

    @Transactional(readOnly = true)
    public List<CuentaPorCobrar> getCuentasPorCobrar(Long idEmpresa) {
        return cpcRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public List<CuentaPorPagar> getCuentasPorPagar(Long idEmpresa) {
        return cppRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional
    public CuentaPorCobrar registrarCobro(Long id, BigDecimal monto, Long idEmpresa, Long idUsuario) {
        CuentaPorCobrar cpc = cpcRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta por cobrar no encontrada"));
        if (!cpc.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: El registro no pertenece a tu empresa");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a cobrar debe ser mayor a cero");
        }
        if (monto.compareTo(cpc.getSaldo()) > 0) {
            throw new IllegalArgumentException("El monto a cobrar (" + monto + ") no puede superar el saldo actual (" + cpc.getSaldo() + ")");
        }

        cpc.setSaldo(cpc.getSaldo().subtract(monto));
        if (cpc.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            cpc.setEstado("PAGADO");
        }
        CuentaPorCobrar guardada = cpcRepository.save(cpc);

        // Generar Asiento Contable por el cobro
        Configuracion config = configuracionService.findByEmpresa(idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la configuración de la empresa"));
        
        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setGlosa("Cobro parcial/total - Factura de Venta Nro: " + cpc.getFacturaVenta().getNroFactura());
        asiento.setEstado(EstadoAsiento.APROBADO);
        asiento.setOrigenDocumento("COBRO_CPC");
        asiento.setOrigenId(cpc.getId());

        List<DetalleAsiento> detalles = new ArrayList<>();

        // DEBE: Caja/Bancos (Ingresa el efectivo)
        DetalleAsiento detCaja = new DetalleAsiento();
        detCaja.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaCaja()));
        detCaja.setDebe(monto);
        detCaja.setHaber(BigDecimal.ZERO);
        detCaja.setAsientoContable(asiento);
        detalles.add(detCaja);

        // HABER: Clientes (Disminuye la cuenta por cobrar)
        DetalleAsiento detClientes = new DetalleAsiento();
        detClientes.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaClientes()));
        detClientes.setDebe(BigDecimal.ZERO);
        detClientes.setHaber(monto);
        detClientes.setAsientoContable(asiento);
        detalles.add(detClientes);

        asiento.setDetalles(detalles);
        asientoService.registrarAsiento(asiento, idEmpresa, idUsuario);

        return guardada;
    }

    @Transactional
    public CuentaPorPagar registrarPago(Long id, BigDecimal monto, Long idEmpresa, Long idUsuario) {
        CuentaPorPagar cpp = cppRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta por pagar no encontrada"));
        if (!cpp.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: El registro no pertenece a tu empresa");
        }

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero");
        }
        if (monto.compareTo(cpp.getSaldo()) > 0) {
            throw new IllegalArgumentException("El monto a pagar (" + monto + ") no puede superar el saldo actual (" + cpp.getSaldo() + ")");
        }

        cpp.setSaldo(cpp.getSaldo().subtract(monto));
        if (cpp.getSaldo().compareTo(BigDecimal.ZERO) == 0) {
            cpp.setEstado("PAGADO");
        }
        CuentaPorPagar guardada = cppRepository.save(cpp);

        // Generar Asiento Contable por el pago
        Configuracion config = configuracionService.findByEmpresa(idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la configuración de la empresa"));

        AsientoContable asiento = new AsientoContable();
        asiento.setFecha(LocalDate.now());
        asiento.setGlosa("Pago parcial/total - Factura de Compra Nro: " + cpp.getFacturaCompra().getNroFactura());
        asiento.setEstado(EstadoAsiento.APROBADO);
        asiento.setOrigenDocumento("PAGO_CPP");
        asiento.setOrigenId(cpp.getId());

        List<DetalleAsiento> detalles = new ArrayList<>();

        // DEBE: Proveedores (Disminuye la cuenta por pagar)
        DetalleAsiento detProv = new DetalleAsiento();
        detProv.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaProveedores()));
        detProv.setDebe(monto);
        detProv.setHaber(BigDecimal.ZERO);
        detProv.setAsientoContable(asiento);
        detalles.add(detProv);

        // HABER: Caja/Bancos (Sale el dinero)
        DetalleAsiento detCaja = new DetalleAsiento();
        detCaja.setCuentaContable(obtenerCuentaReferencial(config.getIdCuentaCaja()));
        detCaja.setDebe(BigDecimal.ZERO);
        detCaja.setHaber(monto);
        detCaja.setAsientoContable(asiento);
        detalles.add(detCaja);

        asiento.setDetalles(detalles);
        asientoService.registrarAsiento(asiento, idEmpresa, idUsuario);

        return guardada;
    }

    private CuentaContable obtenerCuentaReferencial(Long id) {
        CuentaContable ref = new CuentaContable();
        ref.setId(id);
        return ref;
    }
}
