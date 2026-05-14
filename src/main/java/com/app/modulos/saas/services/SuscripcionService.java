package com.app.modulos.saas.services;

import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.repositories.SuscripcionRepository;
import java.math.BigDecimal;
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

	public List<Suscripcion> findByEmpresa(Long idEmpresa) {
		return suscripcionRepository.findByIdEmpresa(idEmpresa);
	}

	public Optional<Suscripcion> findActiveByEmpresa(Long idEmpresa) {
		return suscripcionRepository.findByIdEmpresaAndEstado(idEmpresa, true);
	}

	public Optional<Suscripcion> findById(Long id) {
		return suscripcionRepository.findById(id);
	}

	public Suscripcion subscribe(Long idEmpresa, Long idPlan) {
		// 1. Validar que no tenga otra activa (o desactivar la anterior)
		suscripcionRepository.findByIdEmpresaAndEstado(idEmpresa, true).ifPresent(existing -> {
			existing.setEstado(false);
			suscripcionRepository.save(existing);
		});

		// 2. Obtener el plan
		// Nota: En un entorno real inyectaríamos PlanRepository o PlanService. 
		// Por simplicidad en este refactor, asumo que el objeto Plan ya viene resuelto o se maneja en el Controller.
		// Pero para el Service, lo ideal es recibir el objeto ya listo o los IDs.
		return null; // Este método se terminará de implementar en la siguiente versión o se delega al controller
	}

	public Suscripcion save(Suscripcion suscripcion) {
		// 1. Desactivar suscripciones anteriores de la misma empresa
		if (suscripcion.getIdEmpresa() != null) {
			suscripcionRepository.findByIdEmpresaAndEstado(suscripcion.getIdEmpresa(), true).ifPresent(existing -> {
				existing.setEstado(false);
				suscripcionRepository.save(existing);
			});
		}
		
		// 2. Calcular fechas si no vienen
		if (suscripcion.getFechaInicio() == null) {
			suscripcion.setFechaInicio(java.time.LocalDate.now());
		}
		
		if (suscripcion.getFechaFin() == null && suscripcion.getPlan() != null) {
			// Usar la duración definida en el Plan
			int dias = suscripcion.getPlan().getDuracionDias() != null ? suscripcion.getPlan().getDuracionDias() : 30;
			suscripcion.setFechaFin(suscripcion.getFechaInicio().plusDays(dias));
		}
		
		// 3. Forzar estado activo y asegurar montos (fotografía del precio del plan)
		suscripcion.setEstado(true);
		if (suscripcion.getPlan() != null) {
			BigDecimal precioPlan = suscripcion.getPlan().getPrecio();
			if (suscripcion.getMonto() == null) {
				suscripcion.setMonto(precioPlan);
			}
			if (suscripcion.getMontoPagado() == null) {
				suscripcion.setMontoPagado(precioPlan);
			}
		}

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
			existing.setMontoPagado(input.getMontoPagado());
			existing.setTipoRenovacion(input.getTipoRenovacion());
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
