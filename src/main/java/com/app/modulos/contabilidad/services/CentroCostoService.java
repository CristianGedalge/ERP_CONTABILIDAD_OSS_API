package com.app.modulos.contabilidad.services;

import com.app.modulos.contabilidad.entities.CentroCosto;
import com.app.modulos.contabilidad.repositories.CentroCostoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroCostoService {
    private final CentroCostoRepository centroRepository;

    public CentroCostoService(CentroCostoRepository centroRepository) {
        this.centroRepository = centroRepository;
    }

    @Transactional(readOnly = true)
    public List<CentroCosto> findAllByEmpresa(Long idEmpresa) {
        return centroRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public CentroCosto findById(Long id, Long idEmpresa) {
        CentroCosto cc = centroRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Centro de costo no encontrado"));
        if (idEmpresa != null && !cc.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: El centro de costo no pertenece a tu empresa");
        }
        return cc;
    }

    @Transactional
    public CentroCosto crear(CentroCosto cc, Long idEmpresa) {
        if (cc.getCodigo() == null || cc.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del centro de costo es obligatorio");
        }
        if (cc.getNombre() == null || cc.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del centro de costo es obligatorio");
        }
        if (centroRepository.existsByCodigoAndIdEmpresa(cc.getCodigo(), idEmpresa)) {
            throw new IllegalArgumentException("Ya existe un centro de costo con el código " + cc.getCodigo() + " en esta empresa");
        }
        cc.setIdEmpresa(idEmpresa);
        cc.setEstado(true);
        return centroRepository.save(cc);
    }

    @Transactional
    public CentroCosto actualizar(Long id, CentroCosto ccDetails, Long idEmpresa) {
        CentroCosto cc = findById(id, idEmpresa);
        
        if (ccDetails.getNombre() == null || ccDetails.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del centro de costo es obligatorio");
        }

        // Si cambia de código, verificar unicidad
        if (ccDetails.getCodigo() != null && !ccDetails.getCodigo().equals(cc.getCodigo())) {
            if (centroRepository.existsByCodigoAndIdEmpresa(ccDetails.getCodigo(), idEmpresa)) {
                throw new IllegalArgumentException("Ya existe un centro de costo con el código " + ccDetails.getCodigo() + " en esta empresa");
            }
            cc.setCodigo(ccDetails.getCodigo());
        }

        cc.setNombre(ccDetails.getNombre());
        cc.setDescripcion(ccDetails.getDescripcion());
        if (ccDetails.getEstado() != null) {
            cc.setEstado(ccDetails.getEstado());
        }

        return centroRepository.save(cc);
    }

    @Transactional
    public void eliminar(Long id, Long idEmpresa) {
        CentroCosto cc = findById(id, idEmpresa);
        // Borrado lógico
        cc.setEstado(false);
        centroRepository.save(cc);
    }
}
