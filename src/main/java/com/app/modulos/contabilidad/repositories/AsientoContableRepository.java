package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {
	boolean existsByIdEmpresaAndOrigenDocumentoAndOrigenId(Long idEmpresa, String origenDocumento, Long origenId);
	List<AsientoContable> findByIdEmpresaOrderByFechaDesc(Long idEmpresa);
}
