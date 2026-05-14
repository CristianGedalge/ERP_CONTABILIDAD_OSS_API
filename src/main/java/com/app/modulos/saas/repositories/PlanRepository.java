package com.app.modulos.saas.repositories;

import com.app.modulos.saas.entities.Plan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
	List<Plan> findByEstado(Boolean estado);
}
