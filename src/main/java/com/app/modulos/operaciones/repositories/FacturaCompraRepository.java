package com.app.modulos.operaciones.repositories;

import com.app.modulos.operaciones.entities.FacturaCompra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaCompraRepository extends JpaRepository<FacturaCompra, Long> {
    List<FacturaCompra> findByIdEmpresa(Long idEmpresa);
}
