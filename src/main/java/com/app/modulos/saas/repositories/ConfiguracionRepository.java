package com.app.modulos.saas.repositories;

import com.app.modulos.saas.entities.Configuracion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {
	List<Configuracion> findByIdEmpresa(Long idEmpresa);
	Optional<Configuracion> findFirstByIdEmpresa(Long idEmpresa);
	List<Configuracion> findByEstadoTrue();
}
