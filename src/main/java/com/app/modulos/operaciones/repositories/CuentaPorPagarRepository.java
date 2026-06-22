package com.app.modulos.operaciones.repositories;

import com.app.modulos.operaciones.entities.CuentaPorPagar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaPorPagarRepository extends JpaRepository<CuentaPorPagar, Long> {
    List<CuentaPorPagar> findByIdEmpresa(Long idEmpresa);
    Optional<CuentaPorPagar> findByFacturaCompraId(Long facturaCompraId);
}
