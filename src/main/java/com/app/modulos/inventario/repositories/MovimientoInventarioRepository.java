package com.app.modulos.inventario.repositories;

import com.app.modulos.inventario.entities.MovimientoInventario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByIdEmpresaOrderByFechaDesc(Long idEmpresa);
    List<MovimientoInventario> findByProductoIdAndIdEmpresaOrderByFechaDesc(Long productoId, Long idEmpresa);
}
