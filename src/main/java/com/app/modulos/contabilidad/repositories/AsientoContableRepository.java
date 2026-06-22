package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.AsientoContable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {
    List<AsientoContable> findByIdEmpresa(Long idEmpresa);

    @Query("SELECT COALESCE(MAX(a.nroAsiento), '') FROM AsientoContable a WHERE a.idEmpresa = :idEmpresa AND a.nroAsiento LIKE :prefix")
    String findMaxNroAsientoByPrefix(@Param("idEmpresa") Long idEmpresa, @Param("prefix") String prefix);
}
