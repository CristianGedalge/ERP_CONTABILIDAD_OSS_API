package com.app.modulos.contabilidad.repositories;

import com.app.modulos.contabilidad.entities.PeriodoContable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodoContableRepository extends JpaRepository<PeriodoContable, Long> {
    List<PeriodoContable> findByIdEmpresa(Long idEmpresa);

    @Query("SELECT p FROM PeriodoContable p WHERE p.idEmpresa = :idEmpresa AND :fecha BETWEEN p.fechaInicio AND p.fechaFin")
    Optional<PeriodoContable> findByFechaAndIdEmpresa(@Param("fecha") LocalDate fecha, @Param("idEmpresa") Long idEmpresa);

    @Query("SELECT COUNT(p) > 0 FROM PeriodoContable p WHERE p.idEmpresa = :idEmpresa AND p.estado = 'ABIERTO' AND " +
           "((p.fechaInicio <= :fechaFin AND p.fechaFin >= :fechaInicio))")
    boolean existsOverlappingPeriodoAbierto(@Param("idEmpresa") Long idEmpresa, @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
}
