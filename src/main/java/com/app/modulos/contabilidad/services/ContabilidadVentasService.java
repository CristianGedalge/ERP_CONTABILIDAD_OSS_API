package com.app.modulos.contabilidad.services;

import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.entities.DetalleAsiento;
import com.app.modulos.contabilidad.entities.CuentaContable;
import com.app.modulos.contabilidad.repositories.AsientoContableRepository;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.services.ConfiguracionService;
import com.app.modulos.odoo.services.OdooClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ContabilidadVentasService {

	private final OdooClientService odooClientService;
	private final ConfiguracionService configuracionService;
	private final AsientoContableRepository asientoContableRepository;
	private final CuentaContableRepository cuentaContableRepository;

	public ContabilidadVentasService(
		OdooClientService odooClientService,
		ConfiguracionService configuracionService,
		AsientoContableRepository asientoContableRepository,
		CuentaContableRepository cuentaContableRepository
	) {
		this.odooClientService = odooClientService;
		this.configuracionService = configuracionService;
		this.asientoContableRepository = asientoContableRepository;
		this.cuentaContableRepository = cuentaContableRepository;
	}

	@Transactional
	public int sincronizarVentasOdoo(Long idEmpresa) throws Exception {
		Configuracion config = configuracionService.findByEmpresa(idEmpresa)
			.orElseThrow(() -> new RuntimeException("No se encontró la configuración de Odoo para la empresa ID: " + idEmpresa));

		List<Map<String, Object>> ventasOdoo = odooClientService.obtenerOrdenesVentaPorCompania(config);
		int creados = 0;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for (Map<String, Object> venta : ventasOdoo) {
			Long odooId = ((Number) venta.get("id")).longValue();

			// Validar si ya está registrado
			boolean existe = asientoContableRepository.existsByIdEmpresaAndOrigenDocumentoAndOrigenId(idEmpresa, "sale.order", odooId);
			if (existe) {
				continue;
			}

			String name = (String) venta.get("name");
			Double totalDbl = ((Number) venta.get("amount_total")).doubleValue();
			BigDecimal total = BigDecimal.valueOf(totalDbl).setScale(2, RoundingMode.HALF_UP);

			// Saltar transacciones en cero
			if (total.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			// Calcular montos de contabilidad de Bolivia (IVA 13%, IT 3%)
			BigDecimal ivaDebito = total.multiply(BigDecimal.valueOf(0.13)).setScale(2, RoundingMode.HALF_UP);
			BigDecimal ingresoVentas = total.multiply(BigDecimal.valueOf(0.87)).setScale(2, RoundingMode.HALF_UP);
			BigDecimal gastoIt = total.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
			BigDecimal itPagar = gastoIt; // Para garantizar partida doble exacta

			// Resolver fecha
			LocalDateTime fecha = LocalDateTime.now();
			Object dateObj = venta.get("date_order");
			if (dateObj instanceof String) {
				try {
					fecha = LocalDateTime.parse((String) dateObj, formatter);
				} catch (Exception e) {
					// Fallback a now si falla el parse
				}
			}

			// Obtener o Crear Cuentas Contables Necesarias
			CuentaContable ctaCajaBanco = obtenerOCrearCuenta("1.1.1.01", "Caja/Banco", "ACTIVO", idEmpresa);
			CuentaContable ctaGastoIt = obtenerOCrearCuenta("5.1.4.01", "Impuesto a las Transacciones (IT)", "GASTO", idEmpresa);
			CuentaContable ctaIngresoVentas = obtenerOCrearCuenta("4.1.1.01", "Ingresos por Ventas", "INGRESO", idEmpresa);
			CuentaContable ctaIvaDebito = obtenerOCrearCuenta("2.1.3.01", "Débito Fiscal IVA", "PASIVO", idEmpresa);
			CuentaContable ctaItPagar = obtenerOCrearCuenta("2.1.3.02", "IT por Pagar", "PASIVO", idEmpresa);

			// Crear Asiento Contable
			AsientoContable asiento = new AsientoContable();
			asiento.setFecha(fecha);
			asiento.setGlosa("Registro contable automático - Venta Odoo " + name);
			asiento.setIdEmpresa(idEmpresa);
			asiento.setOrigenDocumento("sale.order");
			asiento.setOrigenId(odooId);

			// Líneas del asiento (Partida doble)
			// DEBE
			DetalleAsiento detCajaBanco = new DetalleAsiento();
			detCajaBanco.setCuenta(ctaCajaBanco);
			detCajaBanco.setDebe(total);
			detCajaBanco.setHaber(BigDecimal.ZERO);
			asiento.addDetalle(detCajaBanco);

			DetalleAsiento detGastoIt = new DetalleAsiento();
			detGastoIt.setCuenta(ctaGastoIt);
			detGastoIt.setDebe(gastoIt);
			detGastoIt.setHaber(BigDecimal.ZERO);
			asiento.addDetalle(detGastoIt);

			// HABER
			DetalleAsiento detIngreso = new DetalleAsiento();
			detIngreso.setCuenta(ctaIngresoVentas);
			detIngreso.setDebe(BigDecimal.ZERO);
			detIngreso.setHaber(ingresoVentas);
			asiento.addDetalle(detIngreso);

			DetalleAsiento detIva = new DetalleAsiento();
			detIva.setCuenta(ctaIvaDebito);
			detIva.setDebe(BigDecimal.ZERO);
			detIva.setHaber(ivaDebito);
			asiento.addDetalle(detIva);

			DetalleAsiento detItPagar = new DetalleAsiento();
			detItPagar.setCuenta(ctaItPagar);
			detItPagar.setDebe(BigDecimal.ZERO);
			detItPagar.setHaber(itPagar);
			asiento.addDetalle(detItPagar);

			asientoContableRepository.save(asiento);
			creados++;
		}

		return creados;
	}

	private CuentaContable obtenerOCrearCuenta(String codigo, String nombre, String tipo, Long idEmpresa) {
		return cuentaContableRepository.findByCodigoAndIdEmpresa(codigo, idEmpresa)
			.orElseGet(() -> {
				CuentaContable nueva = new CuentaContable();
				nueva.setCodigo(codigo);
				nueva.setNombre(nombre);
				nueva.setTipo(tipo);
				nueva.setIdEmpresa(idEmpresa);
				nueva.setEstado(true);
				return cuentaContableRepository.save(nueva);
			});
	}
}
