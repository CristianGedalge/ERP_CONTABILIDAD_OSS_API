package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.CuentaContable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaContableRepository extends JpaRepository<CuentaContable, Long> {
    List<CuentaContable> findByIdEmpresa(Long idEmpresa);
    Optional<CuentaContable> findByCodigoAndIdEmpresa(String codigo, Long idEmpresa);
    boolean existsByCodigoAndIdEmpresa(String codigo, Long idEmpresa);
    List<CuentaContable> findByCuentaPadreId(Long parentId);
}
