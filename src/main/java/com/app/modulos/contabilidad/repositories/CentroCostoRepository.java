package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.CentroCosto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroCostoRepository extends JpaRepository<CentroCosto, Long> {
    List<CentroCosto> findByIdEmpresa(Long idEmpresa);
    Optional<CentroCosto> findByCodigoAndIdEmpresa(String codigo, Long idEmpresa);
    boolean existsByCodigoAndIdEmpresa(String codigo, Long idEmpresa);
}
