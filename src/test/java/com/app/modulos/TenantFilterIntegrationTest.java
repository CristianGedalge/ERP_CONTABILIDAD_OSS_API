package com.app.modulos;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.security.UserPrincipal;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.TipoProducto;
import com.app.modulos.inventario.repositories.ProductoRepository;
import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class TenantFilterIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private TestTenantService testTenantService;

    private Long idEmpresa1;
    private Long idEmpresa2;
    private Producto prodEmpresa1;
    private Producto prodEmpresa2;

    @BeforeEach
    public void setUp() {
        // Limpiar la base de datos de productos de prueba
        productoRepository.deleteAll();

        // Registrar Empresa 1
        Empresa emp1 = new Empresa();
        emp1.setNombre("Empresa de Prueba 1");
        emp1.setEstado(true);
        emp1 = empresaRepository.save(emp1);
        idEmpresa1 = emp1.getId();

        // Registrar Empresa 2
        Empresa emp2 = new Empresa();
        emp2.setNombre("Empresa de Prueba 2");
        emp2.setEstado(true);
        emp2 = empresaRepository.save(emp2);
        idEmpresa2 = emp2.getId();

        // Registrar Producto de la Empresa 1
        prodEmpresa1 = new Producto();
        prodEmpresa1.setCodigo("PROD-101");
        prodEmpresa1.setNombre("Producto Empresa 1");
        prodEmpresa1.setTipo(TipoProducto.PRODUCTO);
        prodEmpresa1.setPrecioVenta(BigDecimal.valueOf(100));
        prodEmpresa1.setCostoUnitario(BigDecimal.valueOf(50));
        prodEmpresa1.setIdEmpresa(idEmpresa1);
        prodEmpresa1.setEstado(true);
        prodEmpresa1 = productoRepository.save(prodEmpresa1);

        // Registrar Producto de la Empresa 2
        prodEmpresa2 = new Producto();
        prodEmpresa2.setCodigo("PROD-102");
        prodEmpresa2.setNombre("Producto Empresa 2");
        prodEmpresa2.setTipo(TipoProducto.PRODUCTO);
        prodEmpresa2.setPrecioVenta(BigDecimal.valueOf(200));
        prodEmpresa2.setCostoUnitario(BigDecimal.valueOf(100));
        prodEmpresa2.setIdEmpresa(idEmpresa2);
        prodEmpresa2.setEstado(true);
        prodEmpresa2 = productoRepository.save(prodEmpresa2);
    }

    private void authenticateAsUser(Long idEmpresa) {
        Usuario usuario = new Usuario();
        usuario.setIdEmpresa(idEmpresa);
        usuario.setCorreo(idEmpresa != null ? "user@empresa" + idEmpresa + ".com" : "superadmin@saas.com");
        usuario.setPassword("password123");
        usuario.setEstado(true);

        UserPrincipal principal = new UserPrincipal(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void whenAuthenticatedAsEmpresa1_thenOnlyEmpresa1ProductsAreReturned() {
        // 1. Autenticar como Empresa 1
        authenticateAsUser(idEmpresa1);

        // 2. Obtener productos a través del servicio interceptado por AOP
        List<Producto> productos = testTenantService.getAllProductsGeneric();

        // 3. Verificar que solo retorne el producto de la Empresa 1
        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getIdEmpresa()).isEqualTo(idEmpresa1);
        assertThat(productos.get(0).getCodigo()).isEqualTo("PROD-101");
    }

    @Test
    public void whenAuthenticatedAsEmpresa2_thenOnlyEmpresa2ProductsAreReturned() {
        // 1. Autenticar como Empresa 2
        authenticateAsUser(idEmpresa2);

        // 2. Obtener productos a través del servicio
        List<Producto> productos = testTenantService.getAllProductsGeneric();

        // 3. Verificar que solo retorne el producto de la Empresa 2
        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getIdEmpresa()).isEqualTo(idEmpresa2);
        assertThat(productos.get(0).getCodigo()).isEqualTo("PROD-102");
    }

    @Test
    public void whenAuthenticatedAsSuperadmin_thenAllProductsAreReturned() {
        // 1. Autenticar como Superadmin (empresaId = null)
        authenticateAsUser(null);

        // 2. Obtener productos a través del servicio
        List<Producto> productos = testTenantService.getAllProductsGeneric();

        // 3. Verificar que retorne todos los productos sin aplicar filtro de tenant
        assertThat(productos).hasSize(2);
        assertThat(productos).extracting(Producto::getCodigo).containsExactlyInAnyOrder("PROD-101", "PROD-102");
    }
}
