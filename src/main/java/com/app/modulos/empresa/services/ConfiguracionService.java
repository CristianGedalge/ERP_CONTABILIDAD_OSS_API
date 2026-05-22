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
			existing.setOdooUrl(input.getOdooUrl());
			existing.setOdooDb(input.getOdooDb());
			existing.setOdooUser(input.getOdooUser());
			existing.setOdooPassword(input.getOdooPassword());
			existing.setOdooCompanyId(input.getOdooCompanyId());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			return configuracionRepository.save(existing);
		});
	}

	public Optional<Configuracion> update(Long id, Configuracion input) {
		return configuracionRepository.findById(id).map(existing -> {
			existing.setIva(input.getIva());
			existing.setIt(input.getIt());
			existing.setMoneda(input.getMoneda());
			existing.setTipoCambio(input.getTipoCambio());
			existing.setOdooUrl(input.getOdooUrl());
			existing.setOdooDb(input.getOdooDb());
			existing.setOdooUser(input.getOdooUser());
			existing.setOdooPassword(input.getOdooPassword());
			existing.setOdooCompanyId(input.getOdooCompanyId());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			if (input.getIdEmpresa() != null) {
				existing.setIdEmpresa(input.getIdEmpresa());
			}
			return configuracionRepository.save(existing);
		});
	}
}
