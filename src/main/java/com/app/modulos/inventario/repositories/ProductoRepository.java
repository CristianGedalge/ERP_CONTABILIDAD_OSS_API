package com.app.modulos.inventario.repositories;

import com.app.modulos.inventario.entities.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByIdEmpresaAndEstadoTrueOrderByIdAsc(Long idEmpresa);
    Optional<Producto> findByIdAndIdEmpresaAndEstadoTrue(Long id, Long idEmpresa);
    Optional<Producto> findByCodigoAndIdEmpresaAndEstadoTrue(String codigo, Long idEmpresa);
    boolean existsByCodigoAndIdEmpresaAndEstadoTrue(String codigo, Long idEmpresa);
}
