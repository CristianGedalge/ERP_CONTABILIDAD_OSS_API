package com.app.modulos.saas.services;

import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.repositories.SuscripcionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SuscripcionService {
	private final SuscripcionRepository suscripcionRepository;

	public SuscripcionService(SuscripcionRepository suscripcionRepository) {
		this.suscripcionRepository = suscripcionRepository;
	}

	public List<Suscripcion> findAll() {
		return suscripcionRepository.findAll();
	}

	public Optional<Suscripcion> findById(Long id) {
		return suscripcionRepository.findById(id);
	}

	public Suscripcion save(Suscripcion suscripcion) {
		return suscripcionRepository.save(suscripcion);
	}

	public Optional<Suscripcion> update(Long id, Suscripcion input) {
		return suscripcionRepository.findById(id).map(existing -> {
			existing.setPlan(input.getPlan());
			existing.setFechaInicio(input.getFechaInicio());
			existing.setFechaFin(input.getFechaFin());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			existing.setMonto(input.getMonto());
			existing.setIdEmpresa(input.getIdEmpresa());
			return suscripcionRepository.save(existing);
		});
	}

	public Optional<Suscripcion> disable(Long id) {
		return suscripcionRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			return suscripcionRepository.save(existing);
		});
	}
}
