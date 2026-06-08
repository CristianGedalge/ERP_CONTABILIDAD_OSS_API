package com.app.modulos.usuario.services;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import com.app.modulos.empresa.repositories.ConfiguracionRepository;
import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.repositories.SuscripcionRepository;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.entities.InfoUsuario;
import com.app.modulos.usuario.entities.AuditLog;
import com.app.modulos.usuario.repositories.RoleRepository;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.usuario.repositories.InfoUsuarioRepository;
import com.app.modulos.usuario.repositories.AuditLogRepository;
import com.app.modulos.usuario.dto.EmpresaBackupDTO;

import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.repositories.ProductoRepository;
import com.app.modulos.inventario.repositories.MovimientoInventarioRepository;

import com.app.modulos.contabilidad.entities.CuentaContable;
import com.app.modulos.contabilidad.entities.CentroCosto;
import com.app.modulos.contabilidad.entities.PeriodoContable;
import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.entities.DetalleAsiento;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import com.app.modulos.contabilidad.repositories.CentroCostoRepository;
import com.app.modulos.contabilidad.repositories.PeriodoContableRepository;
import com.app.modulos.contabilidad.repositories.AsientoContableRepository;

import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.operaciones.entities.DetalleFacturaVenta;
import com.app.modulos.operaciones.entities.FacturaCompra;
import com.app.modulos.operaciones.entities.DetalleFacturaCompra;
import com.app.modulos.operaciones.entities.CuentaPorCobrar;
import com.app.modulos.operaciones.entities.CuentaPorPagar;
import com.app.modulos.operaciones.repositories.FacturaVentaRepository;
import com.app.modulos.operaciones.repositories.FacturaCompraRepository;
import com.app.modulos.operaciones.repositories.CuentaPorCobrarRepository;
import com.app.modulos.operaciones.repositories.CuentaPorPagarRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackupImportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EmpresaRepository empresaRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final InfoUsuarioRepository infoUsuarioRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final CentroCostoRepository centroCostoRepository;
    private final PeriodoContableRepository periodoContableRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final FacturaVentaRepository facturaVentaRepository;
    private final FacturaCompraRepository facturaCompraRepository;
    private final CuentaPorCobrarRepository cuentaPorCobrarRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final AuditLogRepository auditLogRepository;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private interface HibernateMixIn {}

    private static abstract class UsuarioMixIn {
        @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE)
        private String password;
    }

    private static abstract class InfoUsuarioMixIn {
        @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE)
        private Usuario usuario;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .addMixIn(Object.class, HibernateMixIn.class)
            .addMixIn(Usuario.class, UsuarioMixIn.class)
            .addMixIn(InfoUsuario.class, InfoUsuarioMixIn.class);

    public BackupImportService(
            EmpresaRepository empresaRepository,
            ConfiguracionRepository configuracionRepository,
            SuscripcionRepository suscripcionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            InfoUsuarioRepository infoUsuarioRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            CuentaContableRepository cuentaContableRepository,
            CentroCostoRepository centroCostoRepository,
            PeriodoContableRepository periodoContableRepository,
            AsientoContableRepository asientoContableRepository,
            FacturaVentaRepository facturaVentaRepository,
            FacturaCompraRepository facturaCompraRepository,
            CuentaPorCobrarRepository cuentaPorCobrarRepository,
            CuentaPorPagarRepository cuentaPorPagarRepository,
            AuditLogRepository auditLogRepository) {
        this.empresaRepository = empresaRepository;
        this.configuracionRepository = configuracionRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.infoUsuarioRepository = infoUsuarioRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.cuentaContableRepository = cuentaContableRepository;
        this.centroCostoRepository = centroCostoRepository;
        this.periodoContableRepository = periodoContableRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.facturaVentaRepository = facturaVentaRepository;
        this.facturaCompraRepository = facturaCompraRepository;
        this.cuentaPorCobrarRepository = cuentaPorCobrarRepository;
        this.cuentaPorPagarRepository = cuentaPorPagarRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void importarEmpresa(Long idEmpresa, byte[] backupJsonBytes, String currentUsername) {
        try {
            // 1. Deserializar el DTO
            EmpresaBackupDTO dto = objectMapper.readValue(backupJsonBytes, EmpresaBackupDTO.class);

            // Obtener detalles del usuario administrador actual
            Usuario currentUser = userRepository.findByCorreo(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario actual no encontrado: " + currentUsername));
            Long currentUserId = currentUser.getId();
            Long currentUserRoleId = currentUser.getRol() != null ? currentUser.getRol().getId() : null;

            // Desactivar temporalmente el filtro tenantFilter para poder limpiar y re-insertar libremente
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("tenantFilter");

            // 2. FASE DE BORRADO ORDENADO (Evitar FK constraints)
            
            // 2.1 Cuentas por cobrar y pagar
            entityManager.createQuery("DELETE FROM CuentaPorCobrar c WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM CuentaPorPagar c WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.2 Asientos contables: primero borrar detalles, luego cabecera
            entityManager.createQuery("DELETE FROM DetalleAsiento d WHERE d.asientoContable.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM AsientoContable a WHERE a.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.3 Facturas de Venta: detalles y cabecera
            entityManager.createQuery("DELETE FROM DetalleFacturaVenta d WHERE d.facturaVenta.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM FacturaVenta f WHERE f.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.4 Facturas de Compra: detalles y cabecera
            entityManager.createQuery("DELETE FROM DetalleFacturaCompra d WHERE d.facturaCompra.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM FacturaCompra f WHERE f.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.5 Movimientos e Inventario
            entityManager.createQuery("DELETE FROM MovimientoInventario m WHERE m.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM Producto p WHERE p.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.6 Cuentas contables: romper referencias recursivas primero, luego borrar
            entityManager.createQuery("UPDATE CuentaContable c SET c.cuentaPadre = null WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM CuentaContable c WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.7 Estructuras generales
            entityManager.createQuery("DELETE FROM CentroCosto c WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM PeriodoContable p WHERE p.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();

            // 2.8 Usuarios y roles (conservando el administrador que ejecuta la operación)
            entityManager.createQuery("DELETE FROM InfoUsuario i WHERE i.usuario.idEmpresa = :empresaId AND i.usuario.id != :currentUserId")
                    .setParameter("empresaId", idEmpresa)
                    .setParameter("currentUserId", currentUserId).executeUpdate();
            entityManager.createQuery("DELETE FROM Usuario u WHERE u.idEmpresa = :empresaId AND u.id != :currentUserId")
                    .setParameter("empresaId", idEmpresa)
                    .setParameter("currentUserId", currentUserId).executeUpdate();
            
            if (currentUserRoleId != null) {
                entityManager.createQuery("DELETE FROM Rol r WHERE r.idEmpresa = :empresaId AND r.id != :roleId")
                        .setParameter("empresaId", idEmpresa)
                        .setParameter("roleId", currentUserRoleId).executeUpdate();
            } else {
                entityManager.createQuery("DELETE FROM Rol r WHERE r.idEmpresa = :empresaId")
                        .setParameter("empresaId", idEmpresa).executeUpdate();
            }

            // 2.9 Configuraciones, Suscripciones e Historial
            entityManager.createQuery("DELETE FROM Configuracion c WHERE c.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM Suscripcion s WHERE s.idEmpresa = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();
            entityManager.createQuery("DELETE FROM AuditLog a WHERE a.empresaId = :empresaId")
                    .setParameter("empresaId", idEmpresa).executeUpdate();


            // 3. FASE DE RESTAURACIÓN E INSERCIÓN CON REMAPEO
            Map<Long, Rol> roleMap = new HashMap<>();
            Map<Long, Usuario> userMap = new HashMap<>();
            Map<Long, PeriodoContable> periodMap = new HashMap<>();
            Map<Long, CentroCosto> costCenterMap = new HashMap<>();
            Map<Long, CuentaContable> accountMap = new HashMap<>();
            Map<Long, Producto> productMap = new HashMap<>();
            Map<Long, FacturaVenta> invoiceVMap = new HashMap<>();
            Map<Long, FacturaCompra> invoiceCMap = new HashMap<>();

            // 3.1 Configuraciones
            if (dto.getConfiguraciones() != null) {
                for (Configuracion config : dto.getConfiguraciones()) {
                    config.setId(null);
                    config.setIdEmpresa(idEmpresa);
                    configuracionRepository.save(config);
                }
            }

            // 3.2 Suscripciones
            if (dto.getSuscripciones() != null) {
                for (Suscripcion sub : dto.getSuscripciones()) {
                    sub.setId(null);
                    sub.setIdEmpresa(idEmpresa);
                    suscripcionRepository.save(sub);
                }
            }

            // 3.3 Roles
            if (dto.getRoles() != null) {
                for (Rol role : dto.getRoles()) {
                    // Si el rol es global (idEmpresa nulo), no lo duplicamos
                    if (role.getIdEmpresa() == null) {
                        continue;
                    }
                    Long oldId = role.getId();
                    role.setId(null);
                    role.setIdEmpresa(idEmpresa);
                    Rol saved = roleRepository.save(role);
                    roleMap.put(oldId, saved);
                }
            }

            // 3.4 Usuarios
            if (dto.getUsuarios() != null) {
                for (Usuario user : dto.getUsuarios()) {
                    // Si el usuario es el administrador actual, sólo lo mapeamos sin re-insertarlo
                    if (user.getCorreo().equalsIgnoreCase(currentUsername)) {
                        userMap.put(user.getId(), currentUser);
                        continue;
                    }
                    Long oldId = user.getId();
                    user.setId(null);
                    user.setIdEmpresa(idEmpresa);
                    if (user.getRol() != null && user.getRol().getIdEmpresa() != null) {
                        user.setRol(roleMap.get(user.getRol().getId()));
                    }
                    if (user.getPassword() == null || user.getPassword().isBlank()) {
                        // Asignar contraseña hash por defecto para copias antiguas
                        user.setPassword("$2a$10$T1d9jCqD.1y1F8r4w8S9ueS1X/1e3W5k8Pq7sL5L8mQ1.R9y9mQyW");
                    }
                    Usuario saved = userRepository.save(user);
                    userMap.put(oldId, saved);
                }
            }

            // 3.5 InfoUsuarios
            if (dto.getInfoUsuarios() != null) {
                for (InfoUsuario info : dto.getInfoUsuarios()) {
                    if (info.getUsuario() == null) {
                        continue;
                    }
                    Usuario newUser = userMap.get(info.getUsuario().getId());
                    if (newUser != null && !newUser.getCorreo().equalsIgnoreCase(currentUsername)) {
                        info.setId(null);
                        info.setUsuario(newUser);
                        infoUsuarioRepository.save(info);
                    }
                }
            }

            // 3.6 Periodos Contables
            if (dto.getPeriodosContables() != null) {
                for (PeriodoContable period : dto.getPeriodosContables()) {
                    Long oldId = period.getId();
                    period.setId(null);
                    period.setIdEmpresa(idEmpresa);
                    PeriodoContable saved = periodoContableRepository.save(period);
                    periodMap.put(oldId, saved);
                }
            }

            // 3.7 Centros de Costo
            if (dto.getCentrosCosto() != null) {
                for (CentroCosto cc : dto.getCentrosCosto()) {
                    Long oldId = cc.getId();
                    cc.setId(null);
                    cc.setIdEmpresa(idEmpresa);
                    CentroCosto saved = centroCostoRepository.save(cc);
                    costCenterMap.put(oldId, saved);
                }
            }

            // 3.8 Cuentas Contables (Árbol recursivo)
            Map<Long, Long> parentRelationMap = new HashMap<>();
            if (dto.getCuentasContables() != null) {
                // Primera pasada: insertar cuentas con cuentaPadre = null
                for (CuentaContable account : dto.getCuentasContables()) {
                    Long oldId = account.getId();
                    CuentaContable oldParent = account.getCuentaPadre();
                    account.setId(null);
                    account.setIdEmpresa(idEmpresa);
                    account.setCuentaPadre(null);
                    CuentaContable saved = cuentaContableRepository.save(account);
                    accountMap.put(oldId, saved);
                    if (oldParent != null) {
                        parentRelationMap.put(saved.getId(), oldParent.getId());
                    }
                }
                // Segunda pasada: re-enlazar cuentas padres
                for (Map.Entry<Long, Long> entry : parentRelationMap.entrySet()) {
                    CuentaContable acc = cuentaContableRepository.findById(entry.getKey()).orElseThrow();
                    acc.setCuentaPadre(accountMap.get(entry.getValue()));
                    cuentaContableRepository.save(acc);
                }
            }

            // 3.9 Productos
            if (dto.getProductos() != null) {
                for (Producto prod : dto.getProductos()) {
                    Long oldId = prod.getId();
                    prod.setId(null);
                    prod.setIdEmpresa(idEmpresa);
                    Producto saved = productoRepository.save(prod);
                    productMap.put(oldId, saved);
                }
            }

            // 3.10 Movimientos de Inventario
            if (dto.getMovimientosInventario() != null) {
                for (MovimientoInventario mov : dto.getMovimientosInventario()) {
                    mov.setId(null);
                    mov.setIdEmpresa(idEmpresa);
                    if (mov.getProducto() != null) {
                        mov.setProducto(productMap.get(mov.getProducto().getId()));
                    }
                    movimientoInventarioRepository.save(mov);
                }
            }

            // 3.11 Facturas de Venta y sus detalles
            if (dto.getFacturasVenta() != null) {
                for (FacturaVenta inv : dto.getFacturasVenta()) {
                    Long oldId = inv.getId();
                    List<DetalleFacturaVenta> details = new ArrayList<>(inv.getDetalles());
                    inv.setDetalles(new ArrayList<>());
                    inv.setId(null);
                    inv.setIdEmpresa(idEmpresa);
                    FacturaVenta saved = facturaVentaRepository.save(inv);
                    invoiceVMap.put(oldId, saved);

                    for (DetalleFacturaVenta det : details) {
                        det.setId(null);
                        det.setFacturaVenta(saved);
                        if (det.getProducto() != null) {
                            det.setProducto(productMap.get(det.getProducto().getId()));
                        }
                        saved.getDetalles().add(det);
                    }
                    facturaVentaRepository.save(saved);
                }
            }

            // 3.12 Facturas de Compra y sus detalles
            if (dto.getFacturasCompra() != null) {
                for (FacturaCompra inv : dto.getFacturasCompra()) {
                    Long oldId = inv.getId();
                    List<DetalleFacturaCompra> details = new ArrayList<>(inv.getDetalles());
                    inv.setDetalles(new ArrayList<>());
                    inv.setId(null);
                    inv.setIdEmpresa(idEmpresa);
                    FacturaCompra saved = facturaCompraRepository.save(inv);
                    invoiceCMap.put(oldId, saved);

                    for (DetalleFacturaCompra det : details) {
                        det.setId(null);
                        det.setFacturaCompra(saved);
                        if (det.getProducto() != null) {
                            det.setProducto(productMap.get(det.getProducto().getId()));
                        }
                        saved.getDetalles().add(det);
                    }
                    facturaCompraRepository.save(saved);
                }
            }

            // 3.13 Cuentas por Cobrar
            if (dto.getCuentasPorCobrar() != null) {
                for (CuentaPorCobrar cc : dto.getCuentasPorCobrar()) {
                    cc.setId(null);
                    cc.setIdEmpresa(idEmpresa);
                    if (cc.getFacturaVenta() != null) {
                        cc.setFacturaVenta(invoiceVMap.get(cc.getFacturaVenta().getId()));
                    }
                    cuentaPorCobrarRepository.save(cc);
                }
            }

            // 3.14 Cuentas por Pagar
            if (dto.getCuentasPorPagar() != null) {
                for (CuentaPorPagar cp : dto.getCuentasPorPagar()) {
                    cp.setId(null);
                    cp.setIdEmpresa(idEmpresa);
                    if (cp.getFacturaCompra() != null) {
                        cp.setFacturaCompra(invoiceCMap.get(cp.getFacturaCompra().getId()));
                    }
                    cuentaPorPagarRepository.save(cp);
                }
            }

            // 3.15 Asientos Contables y sus detalles
            if (dto.getAsientosContables() != null) {
                for (AsientoContable entry : dto.getAsientosContables()) {
                    List<DetalleAsiento> details = new ArrayList<>(entry.getDetalles());
                    entry.setDetalles(new ArrayList<>());
                    entry.setId(null);
                    entry.setIdEmpresa(idEmpresa);
                    if (entry.getPeriodoContable() != null) {
                        entry.setPeriodoContable(periodMap.get(entry.getPeriodoContable().getId()));
                    }
                    if (entry.getUsuario() != null) {
                        Usuario newUser = userMap.get(entry.getUsuario().getId());
                        entry.setUsuario(newUser != null ? newUser : currentUser);
                    } else {
                        entry.setUsuario(currentUser);
                    }
                    AsientoContable saved = asientoContableRepository.save(entry);

                    for (DetalleAsiento det : details) {
                        det.setId(null);
                        det.setAsientoContable(saved);
                        if (det.getCuentaContable() != null) {
                            det.setCuentaContable(accountMap.get(det.getCuentaContable().getId()));
                        }
                        if (det.getCentroCosto() != null) {
                            det.setCentroCosto(costCenterMap.get(det.getCentroCosto().getId()));
                        }
                        saved.getDetalles().add(det);
                    }
                    asientoContableRepository.save(saved);
                }
            }

            // 3.16 Audit Logs
            if (dto.getAuditLogs() != null) {
                for (AuditLog log : dto.getAuditLogs()) {
                    log.setId(null);
                    log.setEmpresaId(idEmpresa);
                    if (log.getUsuarioId() != null) {
                        Usuario newUser = userMap.get(log.getUsuarioId());
                        log.setUsuarioId(newUser != null ? newUser.getId() : currentUserId);
                    }
                    auditLogRepository.save(log);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al restaurar los datos de la empresa ID: " + idEmpresa, e);
        }
    }
}
