package com.app.modulos;

import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TestTenantService {
    @Autowired
    private ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Producto> getAllProductsGeneric() {
        return productoRepository.findAll();
    }
}
