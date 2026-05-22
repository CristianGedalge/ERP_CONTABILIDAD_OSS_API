package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaContableRepository extends JpaRepository<CuentaContable, Long> {
	Optional<CuentaContable> findByCodigoAndIdEmpresa(String codigo, Long idEmpresa);
	List<CuentaContable> findByIdEmpresaAndEstadoTrueOrderByCodigoAsc(Long idEmpresa);
}
