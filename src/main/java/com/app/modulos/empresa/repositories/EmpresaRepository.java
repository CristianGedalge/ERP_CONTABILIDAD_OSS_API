package com.app.modulos.empresa.repositories;
import com.app.modulos.empresa.entities.Empresa;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findByEstadoTrueOrderByIdAsc();
	Optional<Empresa> findByIdAndEstadoTrue(Long id);
}
