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
import com.app.modulos.usuario.entities.BackupMetadata;
import com.app.modulos.usuario.repositories.RoleRepository;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.usuario.repositories.InfoUsuarioRepository;
import com.app.modulos.usuario.repositories.AuditLogRepository;
import com.app.modulos.usuario.repositories.BackupMetadataRepository;
import com.app.modulos.usuario.dto.EmpresaBackupDTO;

import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.repositories.ProductoRepository;
import com.app.modulos.inventario.repositories.MovimientoInventarioRepository;

import com.app.modulos.contabilidad.entities.CuentaContable;
import com.app.modulos.contabilidad.entities.CentroCosto;
import com.app.modulos.contabilidad.entities.PeriodoContable;
import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.contabilidad.repositories.CuentaContableRepository;
import com.app.modulos.contabilidad.repositories.CentroCostoRepository;
import com.app.modulos.contabilidad.repositories.PeriodoContableRepository;
import com.app.modulos.contabilidad.repositories.AsientoContableRepository;

import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.operaciones.entities.FacturaCompra;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BackupExportService {

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
    private final BackupMetadataRepository backupMetadataRepository;

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

    public BackupExportService(
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
            AuditLogRepository auditLogRepository,
            BackupMetadataRepository backupMetadataRepository) {
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
        this.backupMetadataRepository = backupMetadataRepository;
    }

    @Transactional
    public BackupMetadata exportarEmpresa(Long idEmpresa, String creadoPor) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + idEmpresa));

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "tenant_" + idEmpresa + "_" + timestamp + ".json";
        String backupDir = resolveBackupDirectory();
        String filePath = Paths.get(backupDir, fileName).toAbsolutePath().toString();

        // Registrar metadatos iniciales como PENDIENTE
        BackupMetadata metadata = new BackupMetadata();
        metadata.setIdEmpresa(idEmpresa);
        metadata.setNombreArchivo(fileName);
        metadata.setStorageUrl(filePath);
        metadata.setTamanoBytes(0L);
        metadata.setFechaCreacion(LocalDateTime.now());
        metadata.setEstado("PENDIENTE");
        metadata.setTipo("INDIVIDUAL");
        metadata.setCreadoPor(creadoPor);
        metadata = backupMetadataRepository.save(metadata);

        try {
            // Habilitar explícitamente el filtro tenantFilter para la sesión actual
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("empresaId", idEmpresa);

            // Obtener toda la información de la empresa
            EmpresaBackupDTO dto = new EmpresaBackupDTO();
            dto.setEmpresa(empresa);
            dto.setConfiguraciones(configuracionRepository.findAll());
            dto.setSuscripciones(suscripcionRepository.findByIdEmpresa(idEmpresa));
            dto.setRoles(roleRepository.findByIdEmpresa(idEmpresa));
            dto.setUsuarios(userRepository.findByIdEmpresa(idEmpresa));
            dto.setInfoUsuarios(infoUsuarioRepository.findByUsuarioIdEmpresa(idEmpresa));
            dto.setProductos(productoRepository.findAll());
            dto.setMovimientosInventario(movimientoInventarioRepository.findAll());
            dto.setCuentasContables(cuentaContableRepository.findAll());
            dto.setCentrosCosto(centroCostoRepository.findAll());
            dto.setPeriodosContables(periodoContableRepository.findAll());
            dto.setAsientosContables(asientoContableRepository.findAll());
            dto.setFacturasVenta(facturaVentaRepository.findAll());
            dto.setFacturasCompra(facturaCompraRepository.findAll());
            dto.setCuentasPorCobrar(cuentaPorCobrarRepository.findAll());
            dto.setCuentasPorPagar(cuentaPorPagarRepository.findAll());
            dto.setAuditLogs(auditLogRepository.findByEmpresaIdOrderByFechaHoraDesc(idEmpresa));

            // Crear el directorio de backups si no existe
            Files.createDirectories(Paths.get(backupDir));

            // Guardar a archivo JSON
            File file = new File(filePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, dto);

            // Actualizar metadatos a COMPLETADO
            metadata.setEstado("COMPLETADO");
            metadata.setTamanoBytes(file.length());
            metadata = backupMetadataRepository.save(metadata);

            return metadata;

        } catch (Exception e) {
            // Actualizar metadatos a FALLIDO en caso de error
            metadata.setEstado("FALLIDO");
            backupMetadataRepository.save(metadata);
            throw new RuntimeException("Error al exportar los datos de la empresa ID: " + idEmpresa, e);
        }
    }

    @Transactional
    public BackupMetadata exportarGlobal(String creadoPor) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "global_" + timestamp + ".dump";
        String backupDir = resolveBackupDirectory();
        String filePath = Paths.get(backupDir, fileName).toAbsolutePath().toString();

        // Registrar metadatos iniciales como PENDIENTE
        BackupMetadata metadata = new BackupMetadata();
        metadata.setIdEmpresa(null);
        metadata.setNombreArchivo(fileName);
        metadata.setStorageUrl(filePath);
        metadata.setTamanoBytes(0L);
        metadata.setFechaCreacion(LocalDateTime.now());
        metadata.setEstado("PENDIENTE");
        metadata.setTipo("GLOBAL");
        metadata.setCreadoPor(creadoPor);
        metadata = backupMetadataRepository.save(metadata);

        try {
            // Obtener variables de conexión del entorno
            String dbUrl = System.getenv("DB_URL");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");
            
            String host = "db";
            String dbName = "erp_db";
            if (dbUrl != null) {
                String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
                String[] parts = cleanUrl.split("/");
                if (parts.length > 0) {
                    host = parts[0].split(":")[0];
                }
                if (parts.length > 1) {
                    dbName = parts[1];
                }
            }
            if (username == null) username = "erp";
            if (password == null) password = "Abril2026+++";

            // Crear el directorio de backups si no existe
            Files.createDirectories(Paths.get(backupDir));

            // Determinar si estamos en Windows o Linux/Docker
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                // En Windows dev environment, usamos pg_dump directamente
                pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", "localhost",
                    "-U", username,
                    "-d", dbName,
                    "-F", "c",
                    "-b",
                    "-f", filePath
                );
            } else {
                // En Docker/Linux, usamos pg_dump
                pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-U", username,
                    "-d", dbName,
                    "-F", "c",
                    "-b",
                    "-f", filePath
                );
            }

            pb.environment().put("PGPASSWORD", password);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Leer output para evitar bloqueos del buffer
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("pg_dump falló con código " + exitCode + ". Output: " + output.toString());
            }

            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("El archivo de backup global no fue creado en la ruta: " + filePath);
            }

            // Actualizar metadatos a COMPLETADO
            metadata.setEstado("COMPLETADO");
            metadata.setTamanoBytes(file.length());
            metadata = backupMetadataRepository.save(metadata);

            return metadata;

        } catch (Exception e) {
            metadata.setEstado("FALLIDO");
            backupMetadataRepository.save(metadata);
            throw new RuntimeException("Error al exportar el backup global de la base de datos", e);
        }
    }

    private String resolveBackupDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "./backups";
        } else {
            return "/backups";
        }
    }
}
