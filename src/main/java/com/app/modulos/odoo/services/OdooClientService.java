package com.app.modulos.odoo.services;

import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.entities.Empresa;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OdooClientService {

	@Value("${odoo.master.url:}")
	private String masterUrl;

	@Value("${odoo.master.db:}")
	private String masterDb;

	@Value("${odoo.master.user:}")
	private String masterUser;

	@Value("${odoo.master.password:}")
	private String masterPassword;

	public String getMasterUrl() {
		return masterUrl;
	}

	public String getMasterDb() {
		return masterDb;
	}

	public String getMasterUser() {
		return masterUser;
	}

	public String getMasterPassword() {
		return masterPassword;
	}

	public int crearCompaniaConMaster(Empresa empresa) throws Exception {
		if (masterUrl == null || masterUrl.isEmpty()) {
			throw new IllegalStateException("Las credenciales maestras de Odoo no están configuradas en .env (ODOO_MASTER_URL).");
		}
		Configuracion masterConfig = new Configuracion();
		masterConfig.setOdooUrl(masterUrl);
		masterConfig.setOdooDb(masterDb);
		masterConfig.setOdooUser(masterUser);
		masterConfig.setOdooPassword(masterPassword);
		
		return crearCompania(masterConfig, empresa);
	}

	public int obtenerUid(Configuracion configContable) throws Exception {
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/common"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			configContable.getOdooUser(), 
			configContable.getOdooPassword(),
			new HashMap<String, Object>()
		};
		
		Object result = client.execute("authenticate", params);
		if (result instanceof Integer) {
			return (Integer) result;
		}
		throw new RuntimeException("Autenticación fallida en Odoo.");
	}

	public int crearCompania(Configuracion configContable, Empresa empresa) throws Exception {
		int uid = obtenerUid(configContable);
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		Map<String, Object> camposCompania = new HashMap<>();
		camposCompania.put("name", empresa.getNombre());
		camposCompania.put("currency_id", 63); // ID BOB (Boliviano) en Odoo
		if (empresa.getTelefono() != null && !empresa.getTelefono().isEmpty()) {
			camposCompania.put("phone", empresa.getTelefono());
		}
		if (empresa.getCorreo() != null && !empresa.getCorreo().isEmpty()) {
			camposCompania.put("email", empresa.getCorreo());
		}
		if (empresa.getDireccion() != null && !empresa.getDireccion().isEmpty()) {
			camposCompania.put("street", empresa.getDireccion());
		}
		if (empresa.getNit() != null && !empresa.getNit().isEmpty()) {
			camposCompania.put("vat", empresa.getNit());
		}

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			uid, 
			configContable.getOdooPassword(),
			"res.company", 
			"create", 
			Arrays.asList(camposCompania)
		};
		return (Integer) client.execute("execute_kw", params);
	}

	public int crearUsuarioConMaster(String nombre, String email, String password, int odooCompanyId) throws Exception {
		if (masterUrl == null || masterUrl.isEmpty()) {
			throw new IllegalStateException("Las credenciales maestras de Odoo no están configuradas en .env (ODOO_MASTER_URL).");
		}
		Configuracion masterConfig = new Configuracion();
		masterConfig.setOdooUrl(masterUrl);
		masterConfig.setOdooDb(masterDb);
		masterConfig.setOdooUser(masterUser);
		masterConfig.setOdooPassword(masterPassword);

		return crearUsuario(masterConfig, nombre, email, password, odooCompanyId);
	}

	public int crearUsuario(Configuracion configContable, String nombre, String email, String password, int odooCompanyId) throws Exception {
		int uid = obtenerUid(configContable);
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		// Grupos de acceso administrativo que queremos asignar al usuario
		List<String> xmlIds = Arrays.asList(
			"base.group_user",
			"base.group_erp_manager",
			"sales_team.group_sale_manager",
			"stock.group_stock_manager",
			"account.group_account_manager"
		);
		List<Integer> groupIds = resolverXmlIdsAGroupIds(configContable, uid, xmlIds);

		Map<String, Object> camposUsuario = new HashMap<>();
		camposUsuario.put("name", nombre);
		camposUsuario.put("login", email);
		camposUsuario.put("password", password);
		camposUsuario.put("company_id", odooCompanyId);
		camposUsuario.put("company_ids", Arrays.asList(Arrays.asList(6, 0, Arrays.asList(odooCompanyId))));
		
		// Detectar si el campo de grupos se llama 'group_ids' (Odoo 19+) o 'groups_id' (Odoo 18-)
		String campoGrupos = "groups_id";
		try {
			Object[] paramsFields = new Object[]{
				configContable.getOdooDb(), 
				uid, 
				configContable.getOdooPassword(),
				"res.users", 
				"fields_get", 
				Arrays.asList(Arrays.asList("group_ids", "groups_id")),
				Map.of("attributes", Arrays.asList("type"))
			};
			@SuppressWarnings("unchecked")
			Map<String, Object> fields = (Map<String, Object>) client.execute("execute_kw", paramsFields);
			if (fields != null && fields.containsKey("group_ids")) {
				campoGrupos = "group_ids";
			}
		} catch (Exception e) {
			// Si falla por alguna razón (por ejemplo, método no permitido o versión antigua), usamos por defecto 'groups_id'
		}

		if (!groupIds.isEmpty()) {
			camposUsuario.put(campoGrupos, Arrays.asList(Arrays.asList(6, 0, groupIds)));
		}

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			uid, 
			configContable.getOdooPassword(),
			"res.users", 
			"create", 
			Arrays.asList(camposUsuario)
		};
		return (Integer) client.execute("execute_kw", params);
	}

	private List<Integer> resolverXmlIdsAGroupIds(Configuracion config, int uid, List<String> xmlIds) throws Exception {
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(config.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		List<Integer> resIds = new ArrayList<>();
		for (String xmlId : xmlIds) {
			String[] parts = xmlId.split("\\.");
			if (parts.length == 2) {
				List<Object> filter = Arrays.asList(
					Arrays.asList("module", "=", parts[0]),
					Arrays.asList("name", "=", parts[1])
				);
				Object[] params = new Object[]{
					config.getOdooDb(), uid, config.getOdooPassword(),
					"ir.model.data", "search_read",
					Arrays.asList(filter),
					Map.of("fields", Arrays.asList("res_id"), "limit", 1)
				};
				try {
					Object result = client.execute("execute_kw", params);
					List<Map<String, Object>> records = castToListOfMaps(result);
					if (!records.isEmpty()) {
						Object resIdObj = records.get(0).get("res_id");
						if (resIdObj instanceof Integer) {
							resIds.add((Integer) resIdObj);
						}
					}
				} catch (Exception e) {
					// Ignoramos si algún módulo no está instalado para no impedir el registro
				}
			}
		}
		return resIds;
	}

	public List<Map<String, Object>> obtenerProductosPorCompania(Configuracion configContable) throws Exception {
		int uid = obtenerUid(configContable);
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		Integer companyId = configContable.getOdooCompanyId();
		if (companyId == null) {
			throw new IllegalArgumentException("El odoo_company_id no está configurado.");
		}

		// Filtro: [["company_id", "=", companyId]]
		List<Object> filter = Arrays.asList(
			Arrays.asList("company_id", "=", companyId)
		);

		Map<String, Object> queryOptions = new HashMap<>();
		queryOptions.put("fields", Arrays.asList("id", "name", "list_price", "default_code", "type"));

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			uid, 
			configContable.getOdooPassword(),
			"product.template", 
			"search_read", 
			Arrays.asList(filter),
			queryOptions
		};

		Object result = client.execute("execute_kw", params);
		return castToListOfMaps(result);
	}

	public List<Map<String, Object>> obtenerOrdenesVentaPorCompania(Configuracion configContable) throws Exception {
		int uid = obtenerUid(configContable);
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		Integer companyId = configContable.getOdooCompanyId();
		if (companyId == null) {
			throw new IllegalArgumentException("El odoo_company_id no está configurado.");
		}

		// Filtro: [["company_id", "=", companyId], ["state", "=", "sale"]] (órdenes de venta confirmadas)
		List<Object> filter = Arrays.asList(
			Arrays.asList("company_id", "=", companyId),
			Arrays.asList("state", "=", "sale")
		);

		Map<String, Object> queryOptions = new HashMap<>();
		queryOptions.put("fields", Arrays.asList(
			"id", "name", "date_order", "amount_total", "amount_untaxed", "amount_tax", "state", "partner_id"
		));

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			uid, 
			configContable.getOdooPassword(),
			"sale.order", 
			"search_read", 
			Arrays.asList(filter),
			queryOptions
		};

		Object result = client.execute("execute_kw", params);
		return castToListOfMaps(result);
	}

	public List<Map<String, Object>> obtenerOrdenesCompraPorCompania(Configuracion configContable) throws Exception {
		int uid = obtenerUid(configContable);
		XmlRpcClientConfigImpl configXmlRpc = new XmlRpcClientConfigImpl();
		configXmlRpc.setServerURL(new URL(configContable.getOdooUrl() + "/xmlrpc/2/object"));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(configXmlRpc);

		Integer companyId = configContable.getOdooCompanyId();
		if (companyId == null) {
			throw new IllegalArgumentException("El odoo_company_id no está configurado.");
		}

		// Filtro: [["company_id", "=", companyId], ["state", "in", ["purchase", "done"]]] (órdenes de compra confirmadas/listas)
		List<Object> filter = Arrays.asList(
			Arrays.asList("company_id", "=", companyId),
			Arrays.asList("state", "in", Arrays.asList("purchase", "done"))
		);

		Map<String, Object> queryOptions = new HashMap<>();
		queryOptions.put("fields", Arrays.asList(
			"id", "name", "date_order", "amount_total", "state", "partner_id"
		));

		Object[] params = new Object[]{
			configContable.getOdooDb(), 
			uid, 
			configContable.getOdooPassword(),
			"purchase.order", 
			"search_read", 
			Arrays.asList(filter),
			queryOptions
		};

		Object result = client.execute("execute_kw", params);
		return castToListOfMaps(result);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> castToListOfMaps(Object result) {
		if (result instanceof Object[]) {
			Object[] array = (Object[]) result;
			Map<String, Object>[] mapArray = new Map[array.length];
			for (int i = 0; i < array.length; i++) {
				mapArray[i] = (Map<String, Object>) array[i];
			}
			return Arrays.asList(mapArray);
		} else if (result instanceof List) {
			return (List<Map<String, Object>>) result;
		}
		throw new RuntimeException("Resultado inesperado del servidor Odoo: " + result);
	}
}
