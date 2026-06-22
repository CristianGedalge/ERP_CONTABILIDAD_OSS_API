package com.app.modulos.operaciones.repositories;

import com.app.modulos.operaciones.entities.FacturaVenta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaVentaRepository extends JpaRepository<FacturaVenta, Long> {
    List<FacturaVenta> findByIdEmpresa(Long idEmpresa);

    @Query("SELECT COALESCE(MAX(f.nroFactura), '') FROM FacturaVenta f WHERE f.idEmpresa = :idEmpresa AND f.nroFactura LIKE :prefix")
    String findMaxNroFacturaByPrefix(@Param("idEmpresa") Long idEmpresa, @Param("prefix") String prefix);
}
