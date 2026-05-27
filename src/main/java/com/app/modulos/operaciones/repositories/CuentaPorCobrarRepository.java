package com.app.modulos.operaciones.repositories;

import com.app.modulos.operaciones.entities.CuentaPorCobrar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaPorCobrarRepository extends JpaRepository<CuentaPorCobrar, Long> {
    List<CuentaPorCobrar> findByIdEmpresa(Long idEmpresa);
    Optional<CuentaPorCobrar> findByFacturaVentaId(Long facturaVentaId);
}
