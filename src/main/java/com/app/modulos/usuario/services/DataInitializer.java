package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Permiso;
import com.app.modulos.usuario.repositories.PermisoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

	private final PermisoRepository permisoRepository;

	public DataInitializer(PermisoRepository permisoRepository) {
		this.permisoRepository = permisoRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Map<String, String> requiredPermissions = new HashMap<>();
		requiredPermissions.put("PERM_EMPRESA_READ", "Visualizar datos generales de la empresa");
		requiredPermissions.put("PERM_EMPRESA_WRITE", "Modificar datos generales de la empresa");
		requiredPermissions.put("PERM_USER_READ", "Visualizar empleados y colaboradores");
		requiredPermissions.put("PERM_USER_WRITE", "Crear, editar y dar de baja empleados");
		requiredPermissions.put("PERM_ROL_READ", "Visualizar roles y permisos");
		requiredPermissions.put("PERM_ROL_WRITE", "Crear y editar roles de acceso");
		requiredPermissions.put("PERM_CONFIG_READ", "Visualizar configuraciones globales");
		requiredPermissions.put("PERM_CONFIG_WRITE", "Modificar configuraciones globales");
		requiredPermissions.put("PERM_PRODUCTO_READ", "Visualizar catálogo de productos");
		requiredPermissions.put("PERM_PRODUCTO_WRITE", "Crear y editar productos");
		requiredPermissions.put("PERM_INVENTARIO_READ", "Visualizar movimientos de inventario (Kardex)");
		requiredPermissions.put("PERM_INVENTARIO_WRITE", "Registrar ingresos y salidas de inventario");
		requiredPermissions.put("PERM_CONTABILIDAD_READ", "Visualizar plan de cuentas y reportes contables");
		requiredPermissions.put("PERM_CONTABILIDAD_WRITE", "Crear asientos contables y configurar periodos");
		requiredPermissions.put("PERM_OPERACIONES_READ", "Visualizar facturas de venta, compra y cartera");
		requiredPermissions.put("PERM_OPERACIONES_WRITE", "Registrar y anular facturas de venta, compra y cobros");
		requiredPermissions.put("PERM_SUSCRIPCION_READ", "Visualizar suscripción de la empresa");
		requiredPermissions.put("PERM_SUSCRIPCION_WRITE", "Modificar o cambiar plan de suscripción");
		requiredPermissions.put("PERM_REPORTES_READ", "Visualizar reportes generales del sistema");
		requiredPermissions.put("PERM_REPORTES_WRITE", "Exportar reportes a Excel o PDF");
		requiredPermissions.put("PERM_PANEL_CONTROL_READ", "Visualizar panel de control de branding");
		requiredPermissions.put("PERM_PANEL_CONTROL_WRITE", "Modificar colores y logo en el panel de control");
		requiredPermissions.put("PERM_AUDITORIA_READ", "Visualizar bitácora de auditoría");
		requiredPermissions.put("PERM_AUDITORIA_WRITE", "Exportar o limpiar registros de bitácora");
		requiredPermissions.put("PERM_BACKUP_READ", "Visualizar historial de copias de seguridad");
		requiredPermissions.put("PERM_BACKUP_WRITE", "Crear y descargar copias de seguridad");


		for (Map.Entry<String, String> entry : requiredPermissions.entrySet()) {
			String name = entry.getKey();
			String description = entry.getValue();
			if (!permisoRepository.existsByNombre(name)) {
				Permiso permiso = new Permiso();
				permiso.setNombre(name);
				permiso.setDescripcion(description);
				permisoRepository.save(permiso);
				System.out.println("Seeded permission: " + name);
			}
		}
	}
}
