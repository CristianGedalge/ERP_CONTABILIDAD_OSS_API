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
public class ContabilidadComprasService {

	private final OdooClientService odooClientService;
	private final ConfiguracionService configuracionService;
	private final AsientoContableRepository asientoContableRepository;
	private final CuentaContableRepository cuentaContableRepository;

	public ContabilidadComprasService(
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
	public int sincronizarComprasOdoo(Long idEmpresa) throws Exception {
		Configuracion config = configuracionService.findByEmpresa(idEmpresa)
			.orElseThrow(() -> new RuntimeException("No se encontró la configuración de Odoo para la empresa ID: " + idEmpresa));

		List<Map<String, Object>> comprasOdoo = odooClientService.obtenerOrdenesCompraPorCompania(config);
		int creados = 0;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for (Map<String, Object> compra : comprasOdoo) {
			Long odooId = ((Number) compra.get("id")).longValue();

			// Validar si ya está registrado
			boolean existe = asientoContableRepository.existsByIdEmpresaAndOrigenDocumentoAndOrigenId(idEmpresa, "purchase.order", odooId);
			if (existe) {
				continue;
			}

			String name = (String) compra.get("name");
			Double totalDbl = ((Number) compra.get("amount_total")).doubleValue();
			BigDecimal total = BigDecimal.valueOf(totalDbl).setScale(2, RoundingMode.HALF_UP);

			// Saltar transacciones en cero
			if (total.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			// Calcular montos de contabilidad de Bolivia
			BigDecimal iva = total.multiply(BigDecimal.valueOf(0.13)).setScale(2, RoundingMode.HALF_UP);
			BigDecimal inventario = total.multiply(BigDecimal.valueOf(0.87)).setScale(2, RoundingMode.HALF_UP);

			// Resolver fecha
			LocalDateTime fecha = LocalDateTime.now();
			Object dateObj = compra.get("date_order");
			if (dateObj instanceof String) {
				try {
					fecha = LocalDateTime.parse((String) dateObj, formatter);
				} catch (Exception e) {
					// Fallback a now si falla el parse
				}
			}

			// Obtener o Crear Cuentas Contables Necesarias
			CuentaContable ctaInventario = obtenerOCrearCuenta("1.1.5.01", "Inventario de Mercaderías", "ACTIVO", idEmpresa);
			CuentaContable ctaIvaCredito = obtenerOCrearCuenta("1.1.3.01", "Crédito Fiscal IVA", "ACTIVO", idEmpresa);
			CuentaContable ctaCuentasPagar = obtenerOCrearCuenta("2.1.1.01", "Cuentas por Pagar", "PASIVO", idEmpresa);

			// Crear Asiento Contable
			AsientoContable asiento = new AsientoContable();
			asiento.setFecha(fecha);
			asiento.setGlosa("Registro contable automático - Compra Odoo " + name);
			asiento.setIdEmpresa(idEmpresa);
			asiento.setOrigenDocumento("purchase.order");
			asiento.setOrigenId(odooId);

			// Líneas del asiento (Partida doble)
			// DEBE
			DetalleAsiento detInventario = new DetalleAsiento();
			detInventario.setCuenta(ctaInventario);
			detInventario.setDebe(inventario);
			detInventario.setHaber(BigDecimal.ZERO);
			asiento.addDetalle(detInventario);

			DetalleAsiento detIva = new DetalleAsiento();
			detIva.setCuenta(ctaIvaCredito);
			detIva.setDebe(iva);
			detIva.setHaber(BigDecimal.ZERO);
			asiento.addDetalle(detIva);

			// HABER
			DetalleAsiento detPasivo = new DetalleAsiento();
			detPasivo.setCuenta(ctaCuentasPagar);
			detPasivo.setDebe(BigDecimal.ZERO);
			detPasivo.setHaber(total);
			asiento.addDetalle(detPasivo);

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
