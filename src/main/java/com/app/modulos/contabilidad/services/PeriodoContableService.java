package com.app.modulos.contabilidad.services;

import com.app.modulos.contabilidad.entities.EstadoPeriodo;
import com.app.modulos.contabilidad.entities.PeriodoContable;
import com.app.modulos.contabilidad.repositories.PeriodoContableRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PeriodoContableService {
    private final PeriodoContableRepository periodoRepository;

    public PeriodoContableService(PeriodoContableRepository periodoRepository) {
        this.periodoRepository = periodoRepository;
    }

    @Transactional(readOnly = true)
    public List<PeriodoContable> findAllByEmpresa(Long idEmpresa) {
        return periodoRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public PeriodoContable findById(Long id, Long idEmpresa) {
        PeriodoContable p = periodoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Periodo contable no encontrado"));
        if (idEmpresa != null && !p.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: El periodo no pertenece a tu empresa");
        }
        return p;
    }

    @Transactional
    public PeriodoContable registrarPeriodo(PeriodoContable periodo, Long idEmpresa) {
        if (periodo.getFechaInicio() == null || periodo.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (periodo.getFechaInicio().isAfter(periodo.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        periodo.setIdEmpresa(idEmpresa);
        periodo.setEstado(EstadoPeriodo.ABIERTO);

        boolean overlapping = periodoRepository.existsOverlappingPeriodoAbierto(
            idEmpresa, periodo.getFechaInicio(), periodo.getFechaFin()
        );
        if (overlapping) {
            throw new IllegalArgumentException("Ya existe un periodo contable abierto que se superpone con las fechas ingresadas");
        }

        return periodoRepository.save(periodo);
    }

    @Transactional
    public PeriodoContable cerrarPeriodo(Long id, Long idEmpresa) {
        PeriodoContable p = findById(id, idEmpresa);
        p.setEstado(EstadoPeriodo.CERRADO);
        return periodoRepository.save(p);
    }
}
