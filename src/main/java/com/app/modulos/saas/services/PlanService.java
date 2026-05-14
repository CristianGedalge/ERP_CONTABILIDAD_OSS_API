package com.app.modulos.saas.services;

import com.app.modulos.saas.entities.Plan;
import com.app.modulos.saas.repositories.PlanRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PlanService {
	private final PlanRepository planRepository;

	public PlanService(PlanRepository planRepository) {
		this.planRepository = planRepository;
	}

	public List<Plan> findAll() {
		return planRepository.findAll();
	}

	public List<Plan> findActive() {
		return planRepository.findByEstado(true);
	}

	public Optional<Plan> findById(Long id) {
		return planRepository.findById(id);
	}

	public Plan save(Plan plan) {
		// Aseguramos que las características conozcan su plan
		if (plan.getCaracteristicas() != null) {
			plan.getCaracteristicas().forEach(c -> c.setPlan(plan));
		}
		return planRepository.save(plan);
	}

	public Optional<Plan> update(Long id, Plan input) {
		return planRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setDescripcion(input.getDescripcion());
			existing.setPrecio(input.getPrecio());
			existing.setDuracionDias(input.getDuracionDias());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			
			// Actualizar características
			if (input.getCaracteristicas() != null) {
				existing.getCaracteristicas().clear();
				input.getCaracteristicas().forEach(c -> {
					c.setPlan(existing);
					existing.getCaracteristicas().add(c);
				});
			}
			
			return planRepository.save(existing);
		});
	}

	public Optional<Plan> toggleStatus(Long id) {
		return planRepository.findById(id).map(existing -> {
			existing.setEstado(!existing.getEstado());
			return planRepository.save(existing);
		});
	}

	public void delete(Long id) {
		planRepository.deleteById(id);
	}
}
