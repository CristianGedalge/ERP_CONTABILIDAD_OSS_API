package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.DetalleAsiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleAsientoRepository extends JpaRepository<DetalleAsiento, Long> {
}
