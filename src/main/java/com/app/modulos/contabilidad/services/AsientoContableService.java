package com.app.modulos.contabilidad.services;

import com.app.modulos.contabilidad.entities.*;
import com.app.modulos.contabilidad.repositories.*;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsientoContableService {
    private final AsientoContableRepository asientoRepository;
    private final PeriodoContableRepository periodoRepository;
    private final CuentaContableRepository cuentaRepository;
    private final CentroCostoRepository centroRepository;
    private final UserRepository userRepository;

    public AsientoContableService(
        AsientoContableRepository asientoRepository,
        PeriodoContableRepository periodoRepository,
        CuentaContableRepository cuentaRepository,
        CentroCostoRepository centroRepository,
        UserRepository userRepository
    ) {
        this.asientoRepository = asientoRepository;
        this.periodoRepository = periodoRepository;
        this.cuentaRepository = cuentaRepository;
        this.centroRepository = centroRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AsientoContable> findAllByEmpresa(Long idEmpresa) {
        return asientoRepository.findByIdEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public AsientoContable findById(Long id, Long idEmpresa) {
        AsientoContable asiento = asientoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Asiento contable no encontrado"));
        if (idEmpresa != null && !asiento.getIdEmpresa().equals(idEmpresa)) {
            throw new IllegalArgumentException("Acceso denegado: El asiento no pertenece a tu empresa");
        }
        return asiento;
    }

    @Transactional
    public AsientoContable registrarAsiento(AsientoContable asiento, Long idEmpresa, Long idUsuario) {
        if (asiento.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del asiento es obligatoria");
        }
        if (asiento.getGlosa() == null || asiento.getGlosa().trim().isEmpty()) {
            throw new IllegalArgumentException("La glosa del asiento es obligatoria");
        }
        if (asiento.getDetalles() == null || asiento.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El asiento contable debe tener al menos una línea de detalle");
        }

        // 1. Validar Periodo Contable Abierto
        PeriodoContable periodo = periodoRepository.findByFechaAndIdEmpresa(asiento.getFecha(), idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("No existe un periodo contable registrado para la fecha " + asiento.getFecha()));
        if (periodo.getEstado() != EstadoPeriodo.ABIERTO) {
            throw new IllegalArgumentException("El periodo contable para la fecha " + asiento.getFecha() + " se encuentra CERRADO");
        }

        asiento.setIdEmpresa(idEmpresa);
        asiento.setPeriodoContable(periodo);

        // Asociar creador
        if (idUsuario != null) {
            Usuario u = userRepository.findById(idUsuario).orElse(null);
            asiento.setUsuario(u);
        }

        // 2. Validar y enlazar detalles
        BigDecimal sumDebe = BigDecimal.ZERO;
        BigDecimal sumHaber = BigDecimal.ZERO;

        for (DetalleAsiento detalle : asiento.getDetalles()) {
            detalle.setAsientoContable(asiento);

            // Validar Cuenta Contable
            if (detalle.getCuentaContable() == null || detalle.getCuentaContable().getId() == null) {
                throw new IllegalArgumentException("Cada línea de detalle debe especificar una cuenta contable");
            }
            CuentaContable cuenta = cuentaRepository.findById(detalle.getCuentaContable().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta contable no encontrada: ID " + detalle.getCuentaContable().getId()));
            
            if (!cuenta.getIdEmpresa().equals(idEmpresa)) {
                throw new IllegalArgumentException("La cuenta contable " + cuenta.getCodigo() + " no pertenece a esta empresa");
            }
            if (!Boolean.TRUE.equals(cuenta.getEstado())) {
                throw new IllegalArgumentException("La cuenta contable " + cuenta.getCodigo() + " se encuentra inactiva");
            }
            detalle.setCuentaContable(cuenta);

            // Validar Centro de Costo (opcional)
            if (detalle.getCentroCosto() != null && detalle.getCentroCosto().getId() != null) {
                CentroCosto cc = centroRepository.findById(detalle.getCentroCosto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Centro de costo no encontrado: ID " + detalle.getCentroCosto().getId()));
                if (!cc.getIdEmpresa().equals(idEmpresa)) {
                    throw new IllegalArgumentException("El centro de costo " + cc.getCodigo() + " no pertenece a esta empresa");
                }
                if (!Boolean.TRUE.equals(cc.getEstado())) {
                    throw new IllegalArgumentException("El centro de costo " + cc.getCodigo() + " se encuentra inactivo");
                }
                detalle.setCentroCosto(cc);
            } else {
                detalle.setCentroCosto(null);
            }

            // Normalizar montos
            if (detalle.getDebe() == null) detalle.setDebe(BigDecimal.ZERO);
            if (detalle.getHaber() == null) detalle.setHaber(BigDecimal.ZERO);

            if (detalle.getDebe().compareTo(BigDecimal.ZERO) < 0 || detalle.getHaber().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Los importes del debe y haber no pueden ser negativos");
            }

            sumDebe = sumDebe.add(detalle.getDebe());
            sumHaber = sumHaber.add(detalle.getHaber());
        }

        // 3. Validar Partida Doble si se desea registrar en estado APROBADO
        if (asiento.getEstado() == EstadoAsiento.APROBADO) {
            if (sumDebe.compareTo(sumHaber) != 0) {
                throw new IllegalArgumentException("La suma de cargos (Debe: " + sumDebe + ") debe ser estrictamente igual a la suma de abonos (Haber: " + sumHaber + ") para guardar el asiento en estado APROBADO");
            }
            if (sumDebe.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("El asiento contable no puede registrarse con montos en cero");
            }
            // Generar correlativo oficial si no lo tiene
            if (asiento.getNroAsiento() == null || asiento.getNroAsiento().trim().isEmpty()) {
                asiento.setNroAsiento(generarCorrelativo(idEmpresa, asiento.getFecha()));
            }
        } else {
            // En estado BORRADOR, el correlativo oficial puede quedar vacío o nulo
            asiento.setEstado(EstadoAsiento.BORRADOR);
        }

        return asientoRepository.save(asiento);
    }

    @Transactional
    public AsientoContable actualizarAsiento(Long id, AsientoContable asientoDetails, Long idEmpresa, Long idUsuario) {
        AsientoContable asiento = findById(id, idEmpresa);

        if (asiento.getEstado() == EstadoAsiento.APROBADO) {
            throw new IllegalArgumentException("No se puede modificar un asiento contable que ya ha sido APROBADO");
        }
        if (asiento.getEstado() == EstadoAsiento.ANULADO) {
            throw new IllegalArgumentException("No se puede modificar un asiento contable ANULADO");
        }

        // Copiar propiedades básicas
        asiento.setFecha(asientoDetails.getFecha());
        asiento.setGlosa(asientoDetails.getGlosa());
        asiento.setEstado(asientoDetails.getEstado());
        asiento.setOrigenDocumento(asientoDetails.getOrigenDocumento());
        asiento.setOrigenId(asientoDetails.getOrigenId());

        // Reconciliación de detalles (evita Detached entity passed to persist)
        List<DetalleAsiento> actuales = asiento.getDetalles();
        List<DetalleAsiento> nuevosDetalles = asientoDetails.getDetalles();

        // 1. Eliminar los detalles actuales que ya no vienen en la nueva lista
        actuales.removeIf(act -> nuevosDetalles == null || nuevosDetalles.stream().noneMatch(n -> n.getId() != null && n.getId().equals(act.getId())));

        // 2. Insertar o actualizar los detalles recibidos
        if (nuevosDetalles != null) {
            for (DetalleAsiento detRecibido : nuevosDetalles) {
                if (detRecibido.getId() != null) {
                    // Buscar si ya existía en la colección gestionada
                    DetalleAsiento detExistente = actuales.stream()
                        .filter(act -> act.getId().equals(detRecibido.getId()))
                        .findFirst()
                        .orElse(null);

                    if (detExistente != null) {
                        // Actualizar campos del registro existente
                        detExistente.setDebe(detRecibido.getDebe());
                        detExistente.setHaber(detRecibido.getHaber());
                        detExistente.setCuentaContable(detRecibido.getCuentaContable());
                        detExistente.setCentroCosto(detRecibido.getCentroCosto());
                    } else {
                        // Si tiene un ID pero no existía en los detalles gestionados, lo tratamos como nuevo
                        // ignorando el ID asignado para evitar el error de entidad detached
                        DetalleAsiento detNuevo = new DetalleAsiento();
                        detNuevo.setDebe(detRecibido.getDebe());
                        detNuevo.setHaber(detRecibido.getHaber());
                        detNuevo.setCuentaContable(detRecibido.getCuentaContable());
                        detNuevo.setCentroCosto(detRecibido.getCentroCosto());
                        detNuevo.setAsientoContable(asiento);
                        actuales.add(detNuevo);
                    }
                } else {
                    // Es un detalle completamente nuevo (sin ID)
                    DetalleAsiento detNuevo = new DetalleAsiento();
                    detNuevo.setDebe(detRecibido.getDebe());
                    detNuevo.setHaber(detRecibido.getHaber());
                    detNuevo.setCuentaContable(detRecibido.getCuentaContable());
                    detNuevo.setCentroCosto(detRecibido.getCentroCosto());
                    detNuevo.setAsientoContable(asiento);
                    actuales.add(detNuevo);
                }
            }
        }

        return registrarAsiento(asiento, idEmpresa, idUsuario);
    }

    @Transactional
    public AsientoContable aprobarAsiento(Long id, Long idEmpresa) {
        AsientoContable asiento = findById(id, idEmpresa);

        if (asiento.getEstado() == EstadoAsiento.APROBADO) {
            return asiento; // Ya aprobado
        }
        if (asiento.getEstado() == EstadoAsiento.ANULADO) {
            throw new IllegalArgumentException("No se puede aprobar un asiento contable ANULADO");
        }

        asiento.setEstado(EstadoAsiento.APROBADO);
        // Volvemos a invocar la validación de partida doble y generación de correlativo
        return registrarAsiento(asiento, idEmpresa, asiento.getUsuario() != null ? asiento.getUsuario().getId() : null);
    }

    @Transactional
    public AsientoContable anularAsiento(Long id, Long idEmpresa) {
        AsientoContable asiento = findById(id, idEmpresa);

        if (asiento.getEstado() == EstadoAsiento.ANULADO) {
            return asiento;
        }

        // Si el periodo contable ya está cerrado, no se permite anular el asiento
        PeriodoContable periodo = periodoRepository.findByFechaAndIdEmpresa(asiento.getFecha(), idEmpresa)
            .orElseThrow(() -> new IllegalArgumentException("No existe periodo contable para la fecha del asiento"));
        if (periodo.getEstado() != EstadoPeriodo.ABIERTO) {
            throw new IllegalArgumentException("No se puede anular el asiento porque el periodo contable correspondiente se encuentra CERRADO");
        }

        asiento.setEstado(EstadoAsiento.ANULADO);
        return asientoRepository.save(asiento);
    }

    private synchronized String generarCorrelativo(Long idEmpresa, LocalDate fecha) {
        // Formato: AS-YYYY-MM-NNNN
        String prefix = String.format("AS-%04d-%02d-", fecha.getYear(), fecha.getMonthValue());
        String maxNro = asientoRepository.findMaxNroAsientoByPrefix(idEmpresa, prefix + "%");
        
        int nextCorrelative = 1;
        if (maxNro != null && !maxNro.trim().isEmpty() && maxNro.startsWith(prefix)) {
            try {
                String suffix = maxNro.substring(prefix.length());
                nextCorrelative = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException e) {
                // Si falla el parseo, se mantiene en 1
            }
        }
        
        return String.format("%s%04d", prefix, nextCorrelative);
    }
}
