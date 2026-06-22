package com.app.modulos.contabilidad.services;

import com.app.modulos.contabilidad.entities.CuentaContable;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaContableService {
    private final CuentaContableRepository cuentaRepository;

    public CuentaContableService(CuentaContableRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaContable> findAllByEmpresa(Long idEmpresa) {
        return cuentaRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public CuentaContable findById(Long id, Long idEmpresa) {
        CuentaContable cuenta = cuentaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cuenta contable no encontrada"));
        if (idEmpresa != null && !cuenta.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: La cuenta no pertenece a tu empresa");
        }
        return cuenta;
    }

    @Transactional
    public CuentaContable crear(CuentaContable cuenta, Long idEmpresa) {
        if (cuenta.getCodigo() == null || cuenta.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la cuenta es obligatorio");
        }
        if (cuenta.getNombre() == null || cuenta.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cuenta es obligatorio");
        }
        if (cuentaRepository.existsByCodigoAndIdEmpresa(cuenta.getCodigo(), idEmpresa)) {
            throw new IllegalArgumentException("Ya existe una cuenta contable con el código " + cuenta.getCodigo() + " en esta empresa");
        }

        cuenta.setIdEmpresa(idEmpresa);
        cuenta.setEstado(true);

        if (cuenta.getCuentaPadre() != null && cuenta.getCuentaPadre().getId() != null) {
            CuentaContable padre = findById(cuenta.getCuentaPadre().getId(), idEmpresa);
            cuenta.setCuentaPadre(padre);
            cuenta.setTipo(padre.getTipo()); // Hereda naturaleza
            cuenta.setNivel(padre.getNivel() + 1);
        } else {
            cuenta.setCuentaPadre(null);
            cuenta.setNivel(1);
            if (cuenta.getTipo() == null) {
                throw new IllegalArgumentException("El tipo de cuenta (ACTIVO, PASIVO, etc.) es obligatorio para cuentas principales");
            }
        }

        return cuentaRepository.save(cuenta);
    }

    @Transactional
    public CuentaContable actualizar(Long id, CuentaContable cuentaDetails, Long idEmpresa) {
        CuentaContable cuenta = findById(id, idEmpresa);

        if (cuentaDetails.getNombre() == null || cuentaDetails.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cuenta es obligatorio");
        }

        if (cuentaDetails.getCodigo() != null && !cuentaDetails.getCodigo().equals(cuenta.getCodigo())) {
            if (cuentaRepository.existsByCodigoAndIdEmpresa(cuentaDetails.getCodigo(), idEmpresa)) {
                throw new IllegalArgumentException("Ya existe una cuenta contable con el código " + cuentaDetails.getCodigo() + " en esta empresa");
            }
            cuenta.setCodigo(cuentaDetails.getCodigo());
        }

        cuenta.setNombre(cuentaDetails.getNombre());
        if (cuentaDetails.getEstado() != null) {
            cuenta.setEstado(cuentaDetails.getEstado());
        }

        // Si es cuenta principal, puede cambiar tipo
        if (cuenta.getCuentaPadre() == null && cuentaDetails.getTipo() != null) {
            cuenta.setTipo(cuentaDetails.getTipo());
            // Propagar tipo a subcuentas de forma recursiva
            propagarTipo(cuenta, cuentaDetails.getTipo());
        }

        return cuentaRepository.save(cuenta);
    }

    private void propagarTipo(CuentaContable padre, com.app.modulos.contabilidad.entities.TipoCuenta tipo) {
        List<CuentaContable> subCuentas = cuentaRepository.findByCuentaPadreId(padre.getId());
        for (CuentaContable hijo : subCuentas) {
            hijo.setTipo(tipo);
            cuentaRepository.save(hijo);
            propagarTipo(hijo, tipo);
        }
    }

    @Transactional
    public void eliminar(Long id, Long idEmpresa) {
        CuentaContable cuenta = findById(id, idEmpresa);
        List<CuentaContable> subCuentas = cuentaRepository.findByCuentaPadreId(id);
        if (!subCuentas.isEmpty()) {
            throw new IllegalArgumentException("No se puede dar de baja una cuenta contable que tiene subcuentas activas");
        }
        // Desactivación lógica
        cuenta.setEstado(false);
        cuentaRepository.save(cuenta);
    }
}
