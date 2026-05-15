package com.app.modulos.saas.repositories;

import com.app.modulos.saas.entities.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
	List<Suscripcion> findByIdEmpresa(Long idEmpresa);
	Optional<Suscripcion> findByIdEmpresaAndEstado(Long idEmpresa, Boolean estado);
}
