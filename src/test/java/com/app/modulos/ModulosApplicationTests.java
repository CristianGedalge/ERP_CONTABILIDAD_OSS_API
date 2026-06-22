package com.app.modulos;

import com.app.modulos.inventario.controllers.ProductoController;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.TipoProducto;
import com.app.modulos.inventario.repositories.ProductoRepository;
import com.app.modulos.usuario.entities.AuditLog;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.security.UserPrincipal;
import com.app.modulos.usuario.repositories.AuditLogRepository;
import com.app.modulos.usuario.repositories.RoleRepository;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import com.app.modulos.usuario.entities.BackupMetadata;
import com.app.modulos.usuario.repositories.BackupMetadataRepository;
import com.app.modulos.usuario.controllers.BackupController;
import com.app.modulos.usuario.services.BackupExportService;
import com.app.modulos.usuario.services.BackupImportService;
import com.app.modulos.usuario.dto.EmpresaBackupDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ModulosApplicationTests {

	@Autowired
	private BackupExportService backupExportService;

	@Autowired
	private BackupImportService backupImportService;

	@Autowired
	private BackupMetadataRepository backupMetadataRepository;

	@Autowired
	private BackupController backupController;

	@Autowired
	private ProductoController productoController;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Test
	void testAuditLogCreatedOnProductCreation() throws Exception {
		// Create company
		Empresa empresa = new Empresa();
		empresa.setNombre("Test Company Audit");
		empresa.setEstado(true);
		empresa = empresaRepository.save(empresa);

		// Find or create SUPERADMIN role
		Rol rol = roleRepository.findAll().stream()
				.filter(r -> "SUPERADMIN".equals(r.getNombre()))
				.findFirst()
				.orElseGet(() -> {
					Rol newRol = new Rol();
					newRol.setNombre("SUPERADMIN");
					newRol.setDescripcion("Super Administrator");
					newRol.setEstado(true);
					return roleRepository.save(newRol);
				});

		// Create user with unique email and username
		String uuidStr = UUID.randomUUID().toString().substring(0, 8);
		String email = "testaudit_" + uuidStr + "@correo.com";
		String username = "testaudit_" + uuidStr;

		Usuario usuario = new Usuario();
		usuario.setIdEmpresa(empresa.getId());
		usuario.setCorreo(email);
		usuario.setUsername(username);
		usuario.setPassword("password123");
		usuario.setEstado(true);
		usuario.setRol(rol);
		usuario = userRepository.save(usuario);

		UserPrincipal principal = new UserPrincipal(usuario);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		// Clear logs
		auditLogRepository.deleteAll();

		// Create product
		Producto p = new Producto();
		p.setCodigo("PROD-AUD-" + uuidStr);
		p.setNombre("Test Product " + uuidStr);
		p.setTipo(TipoProducto.PRODUCTO);
		p.setPrecioVenta(BigDecimal.valueOf(100));
		p.setCostoUnitario(BigDecimal.valueOf(50));
		p.setIdEmpresa(empresa.getId());
		p.setEstado(true);

		ResponseEntity<?> response = productoController.create(p, principal);
		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

		// Wait for async log
		Thread.sleep(1000);

		List<AuditLog> logs = auditLogRepository.findAll();
		assertThat(logs).isNotEmpty();
		AuditLog log = logs.get(0);
		assertThat(log.getModulo()).isEqualTo("INVENTARIO");
		assertThat(log.getAccion()).isEqualTo("CREAR");
		assertThat(log.getUsuarioNombre()).isEqualTo(email);
		assertThat(log.getResultado()).isEqualTo("EXITO");
	}

	@Test
	void testBackupMetadataPersistenceAndController() throws Exception {
		// Create company
		Empresa empresa = new Empresa();
		empresa.setNombre("Test Company Backup");
		empresa.setEstado(true);
		empresa = empresaRepository.save(empresa);

		// Find or create SUPERADMIN role
		Rol superadminRol = roleRepository.findAll().stream()
				.filter(r -> "SUPERADMIN".equals(r.getNombre()))
				.findFirst()
				.orElseGet(() -> {
					Rol newRol = new Rol();
					newRol.setNombre("SUPERADMIN");
					newRol.setDescripcion("Super Administrator");
					newRol.setEstado(true);
					return roleRepository.save(newRol);
				});

		// Find or create ADMIN role
		Rol adminRol = roleRepository.findAll().stream()
				.filter(r -> "ADMIN".equals(r.getNombre()))
				.findFirst()
				.orElseGet(() -> {
					Rol newRol = new Rol();
					newRol.setNombre("ADMIN");
					newRol.setDescripcion("Administrator");
					newRol.setEstado(true);
					return roleRepository.save(newRol);
				});

		// Create superadmin user
		String uuidStr = UUID.randomUUID().toString().substring(0, 8);
		Usuario superadminUser = new Usuario();
		superadminUser.setCorreo("super_" + uuidStr + "@correo.com");
		superadminUser.setUsername("super_" + uuidStr);
		superadminUser.setPassword("password123");
		superadminUser.setEstado(true);
		superadminUser.setRol(superadminRol);
		superadminUser = userRepository.save(superadminUser);

		// Create admin user for company
		Usuario adminUser = new Usuario();
		adminUser.setIdEmpresa(empresa.getId());
		adminUser.setCorreo("admin_" + uuidStr + "@correo.com");
		adminUser.setUsername("admin_" + uuidStr);
		adminUser.setPassword("password123");
		adminUser.setEstado(true);
		adminUser.setRol(adminRol);
		adminUser = userRepository.save(adminUser);

		// Clear existing metadata backups
		backupMetadataRepository.deleteAll();

		// Save a global backup metadata
		BackupMetadata globalBackup = new BackupMetadata();
		globalBackup.setIdEmpresa(null);
		globalBackup.setNombreArchivo("global_" + uuidStr + ".dump");
		globalBackup.setStorageUrl("/backups/global_" + uuidStr + ".dump");
		globalBackup.setTamanoBytes(102400L);
		globalBackup.setFechaCreacion(LocalDateTime.now());
		globalBackup.setEstado("COMPLETADO");
		globalBackup.setTipo("GLOBAL");
		globalBackup.setCreadoPor(superadminUser.getCorreo());
		backupMetadataRepository.save(globalBackup);

		// Save a tenant-specific backup metadata
		BackupMetadata tenantBackup = new BackupMetadata();
		tenantBackup.setIdEmpresa(empresa.getId());
		tenantBackup.setNombreArchivo("tenant_" + uuidStr + ".dump");
		tenantBackup.setStorageUrl("/backups/tenant_" + uuidStr + ".dump");
		tenantBackup.setTamanoBytes(51200L);
		tenantBackup.setFechaCreacion(LocalDateTime.now());
		tenantBackup.setEstado("COMPLETADO");
		tenantBackup.setTipo("INDIVIDUAL");
		tenantBackup.setCreadoPor(adminUser.getCorreo());
		backupMetadataRepository.save(tenantBackup);

		// 1. Verify Superadmin retrieves both backups
		UserPrincipal superPrincipal = new UserPrincipal(superadminUser);
		Authentication superAuth = new UsernamePasswordAuthenticationToken(superPrincipal, null, superPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(superAuth);
		ResponseEntity<List<BackupMetadata>> superResponse = backupController.getBackups(superPrincipal);
		assertThat(superResponse.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(superResponse.getBody()).hasSize(2);

		// 2. Verify Company Admin retrieves only their company's backup
		UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
		Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(adminAuth);
		ResponseEntity<List<BackupMetadata>> adminResponse = backupController.getBackups(adminPrincipal);
		assertThat(adminResponse.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(adminResponse.getBody()).hasSize(1);
		assertThat(adminResponse.getBody().get(0).getNombreArchivo()).isEqualTo("tenant_" + uuidStr + ".dump");

		// Clear context after test
		SecurityContextHolder.clearContext();
	}

	@Test
	void testBackupExportAndDownload() throws Exception {
		// Create company
		Empresa empresa = new Empresa();
		empresa.setNombre("Export Test Company");
		empresa.setEstado(true);
		empresa = empresaRepository.save(empresa);

		// Find or create ADMIN role
		Rol adminRol = roleRepository.findAll().stream()
				.filter(r -> "ADMIN".equals(r.getNombre()))
				.findFirst()
				.orElseGet(() -> {
					Rol newRol = new Rol();
					newRol.setNombre("ADMIN");
					newRol.setDescripcion("Administrator");
					newRol.setEstado(true);
					return roleRepository.save(newRol);
				});

		// Create admin user for company
		String uuidStr = UUID.randomUUID().toString().substring(0, 8);
		Usuario adminUser = new Usuario();
		adminUser.setIdEmpresa(empresa.getId());
		adminUser.setCorreo("admin_exp_" + uuidStr + "@correo.com");
		adminUser.setUsername("admin_exp_" + uuidStr);
		adminUser.setPassword("password123");
		adminUser.setEstado(true);
		adminUser.setRol(adminRol);
		adminUser = userRepository.save(adminUser);

		// Authenticate as Admin of this company
		UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
		Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(adminAuth);

		// 1. Trigger export via Controller
		ResponseEntity<BackupMetadata> exportResponse = backupController.exportar(adminPrincipal, null);
		assertThat(exportResponse.getStatusCode().is2xxSuccessful()).isTrue();
		BackupMetadata metadata = exportResponse.getBody();
		assertThat(metadata).isNotNull();
		assertThat(metadata.getEstado()).isEqualTo("COMPLETADO");
		assertThat(metadata.getTipo()).isEqualTo("INDIVIDUAL");
		assertThat(metadata.getIdEmpresa()).isEqualTo(empresa.getId());

		// Verify file exists physically
		File backupFile = new File(metadata.getStorageUrl());
		assertThat(backupFile.exists()).isTrue();

		// Read and deserialize export file to verify content
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
		EmpresaBackupDTO backupData = mapper.readValue(backupFile, EmpresaBackupDTO.class);
		assertThat(backupData.getEmpresa().getNombre()).isEqualTo("Export Test Company");

		// 2. Verify download endpoint
		ResponseEntity<Resource> downloadResponse = backupController.descargar(adminPrincipal, metadata.getId());
		assertThat(downloadResponse.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(downloadResponse.getBody()).isNotNull();
		assertThat(downloadResponse.getBody().exists()).isTrue();

		// Clean up files and context after test
		if (backupFile.exists()) {
			backupFile.delete();
		}
		SecurityContextHolder.clearContext();
	}

	@Test
	void testBackupRestoration() throws Exception {
		// 1. Create company
		Empresa empresa = new Empresa();
		empresa.setNombre("Restoration Test Company");
		empresa.setEstado(true);
		empresa = empresaRepository.save(empresa);
		final Long empresaId = empresa.getId();

		// Find or create SUPERADMIN role
		Rol superadminRol = roleRepository.findAll().stream()
				.filter(r -> "SUPERADMIN".equals(r.getNombre()))
				.findFirst()
				.orElseGet(() -> {
					Rol newRol = new Rol();
					newRol.setNombre("SUPERADMIN");
					newRol.setDescripcion("Super Administrator");
					newRol.setEstado(true);
					return roleRepository.save(newRol);
				});

		// Create admin user for company
		String uuidStr = UUID.randomUUID().toString().substring(0, 8);
		Usuario adminUser = new Usuario();
		adminUser.setIdEmpresa(empresaId);
		adminUser.setCorreo("admin_rest_" + uuidStr + "@correo.com");
		adminUser.setUsername("admin_rest_" + uuidStr);
		adminUser.setPassword("password123");
		adminUser.setEstado(true);
		adminUser.setRol(superadminRol);
		adminUser = userRepository.save(adminUser);

		// Authenticate as Admin of this company
		UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
		Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(adminAuth);

		// Create some data to backup (e.g. a product)
		Producto p = new Producto();
		p.setCodigo("PROD-RST-" + uuidStr);
		p.setNombre("Original Product Name");
		p.setTipo(TipoProducto.PRODUCTO);
		p.setPrecioVenta(BigDecimal.valueOf(120));
		p.setCostoUnitario(BigDecimal.valueOf(60));
		p.setIdEmpresa(empresaId);
		p.setEstado(true);
		
		ResponseEntity<?> createResponse = productoController.create(p, adminPrincipal);
		assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();

		// Trigger export via Controller
		ResponseEntity<BackupMetadata> exportResponse = backupController.exportar(adminPrincipal, empresaId);
		assertThat(exportResponse.getStatusCode().is2xxSuccessful()).isTrue();
		BackupMetadata metadata = exportResponse.getBody();
		assertThat(metadata).isNotNull();

		File backupFile = new File(metadata.getStorageUrl());
		assertThat(backupFile.exists()).isTrue();

		// Modify/corrupt the product in database
		List<Producto> productsBefore = productoRepository.findAll().stream()
				.filter(prod -> prod.getIdEmpresa().equals(empresaId))
				.toList();
		assertThat(productsBefore).hasSize(1);
		Producto prodToModify = productsBefore.get(0);
		prodToModify.setNombre("Modified Product Name");
		productoRepository.save(prodToModify);

		// Verify change in DB
		Producto modifiedProd = productoRepository.findById(prodToModify.getId()).orElseThrow();
		assertThat(modifiedProd.getNombre()).isEqualTo("Modified Product Name");

		// Read bytes of backup JSON file
		byte[] jsonBytes = java.nio.file.Files.readAllBytes(backupFile.toPath());

		// 2. Perform restoration
		backupImportService.importarEmpresa(empresaId, jsonBytes, adminUser.getCorreo());

		// Verify data is restored to original state (Original Product Name)
		List<Producto> productsAfter = productoRepository.findAll().stream()
				.filter(prod -> prod.getIdEmpresa().equals(empresaId))
				.toList();
		assertThat(productsAfter).hasSize(1);
		assertThat(productsAfter.get(0).getNombre()).isEqualTo("Original Product Name");

		// Clean up files and context after test
		if (backupFile.exists()) {
			backupFile.delete();
		}
		SecurityContextHolder.clearContext();
	}
}
