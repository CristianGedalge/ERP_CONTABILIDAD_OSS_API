package com.app.modulos.saas.services;

import com.app.modulos.saas.entities.Configuracion;
import com.app.modulos.saas.repositories.ConfiguracionRepository;
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

	public Optional<Configuracion> findById(Long id) {
		return configuracionRepository.findById(id);
	}

	public Configuracion save(Configuracion configuracion) {
		return configuracionRepository.save(configuracion);
	}

	public Optional<Configuracion> update(Long id, Configuracion input) {
		return configuracionRepository.findById(id).map(existing -> {
			existing.setIva(input.getIva());
			existing.setIt(input.getIt());
			existing.setMoneda(input.getMoneda());
			existing.setTipoCambio(input.getTipoCambio());
			existing.setIdEmpresa(input.getIdEmpresa());
			return configuracionRepository.save(existing);
		});
	}
}
