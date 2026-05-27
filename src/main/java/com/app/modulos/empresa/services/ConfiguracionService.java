package com.app.modulos.empresa.services;

import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.repositories.ConfiguracionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionService {
	private final ConfiguracionRepository configuracionRepository;

	public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
		this.configuracionRepository = configuracionRepository;
	}

	public List<Configuracion> findAll() {
		return configuracionRepository.findAll();
	}

	public List<Configuracion> findAllActivas() {
		return configuracionRepository.findByEstadoTrue();
	}

	public List<Configuracion> findAllByEmpresa(Long idEmpresa) {
		return configuracionRepository.findByIdEmpresa(idEmpresa);
	}

	public Optional<Configuracion> findByEmpresa(Long idEmpresa) {
		return configuracionRepository.findFirstByIdEmpresa(idEmpresa);
	}

	public Optional<Configuracion> findById(Long id) {
		return configuracionRepository.findById(id);
	}

	public Configuracion save(Configuracion configuracion) {
		return configuracionRepository.save(configuracion);
	}

	public Optional<Configuracion> updateByEmpresa(Long idEmpresa, Configuracion input) {
		return configuracionRepository.findFirstByIdEmpresa(idEmpresa).map(existing -> {
			existing.setIva(input.getIva());
			existing.setIt(input.getIt());
			existing.setMoneda(input.getMoneda());
			existing.setTipoCambio(input.getTipoCambio());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			existing.setIdCuentaCaja(input.getIdCuentaCaja());
			existing.setIdCuentaClientes(input.getIdCuentaClientes());
			existing.setIdCuentaProveedores(input.getIdCuentaProveedores());
			existing.setIdCuentaVentas(input.getIdCuentaVentas());
			existing.setIdCuentaCompras(input.getIdCuentaCompras());
			existing.setIdCuentaIvaDebito(input.getIdCuentaIvaDebito());
			existing.setIdCuentaIvaCredito(input.getIdCuentaIvaCredito());
			existing.setIdCuentaItGasto(input.getIdCuentaItGasto());
			existing.setIdCuentaItPasivo(input.getIdCuentaItPasivo());
			existing.setIdCuentaInventario(input.getIdCuentaInventario());
			existing.setIdCuentaCostoVentas(input.getIdCuentaCostoVentas());
			return configuracionRepository.save(existing);
		});
	}

	public Optional<Configuracion> update(Long id, Configuracion input) {
		return configuracionRepository.findById(id).map(existing -> {
			existing.setIva(input.getIva());
			existing.setIt(input.getIt());
			existing.setMoneda(input.getMoneda());
			existing.setTipoCambio(input.getTipoCambio());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			if (input.getIdEmpresa() != null) {
				existing.setIdEmpresa(input.getIdEmpresa());
			}
			existing.setIdCuentaCaja(input.getIdCuentaCaja());
			existing.setIdCuentaClientes(input.getIdCuentaClientes());
			existing.setIdCuentaProveedores(input.getIdCuentaProveedores());
			existing.setIdCuentaVentas(input.getIdCuentaVentas());
			existing.setIdCuentaCompras(input.getIdCuentaCompras());
			existing.setIdCuentaIvaDebito(input.getIdCuentaIvaDebito());
			existing.setIdCuentaIvaCredito(input.getIdCuentaIvaCredito());
			existing.setIdCuentaItGasto(input.getIdCuentaItGasto());
			existing.setIdCuentaItPasivo(input.getIdCuentaItPasivo());
			existing.setIdCuentaInventario(input.getIdCuentaInventario());
			existing.setIdCuentaCostoVentas(input.getIdCuentaCostoVentas());
			return configuracionRepository.save(existing);
		});
	}
}
