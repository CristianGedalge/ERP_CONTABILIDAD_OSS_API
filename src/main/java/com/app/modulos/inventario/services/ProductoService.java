package com.app.modulos.inventario.services;

import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.repositories.ProductoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> findAllByEmpresa(Long idEmpresa) {
        return productoRepository.findByIdEmpresaAndEstadoTrueOrderByIdAsc(idEmpresa);
    }

    public Optional<Producto> findById(Long id, Long idEmpresa) {
        return productoRepository.findByIdAndIdEmpresaAndEstadoTrue(id, idEmpresa);
    }

    public boolean existsByCodigo(String codigo, Long idEmpresa) {
        return productoRepository.existsByCodigoAndIdEmpresaAndEstadoTrue(codigo, idEmpresa);
    }

    @Transactional
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> update(Long id, Producto input, Long idEmpresa) {
        return productoRepository.findByIdAndIdEmpresaAndEstadoTrue(id, idEmpresa).map(existing -> {
            existing.setCodigo(input.getCodigo());
            existing.setNombre(input.getNombre());
            existing.setDescripcion(input.getDescripcion());
            existing.setTipo(input.getTipo());
            existing.setPrecioVenta(input.getPrecioVenta());
            existing.setCostoUnitario(input.getCostoUnitario());
            existing.setUnidadMedida(input.getUnidadMedida());
            if (input.getEstado() != null) {
                existing.setEstado(input.getEstado());
            }
            return productoRepository.save(existing);
        });
    }

    @Transactional
    public Optional<Producto> disable(Long id, Long idEmpresa) {
        return productoRepository.findByIdAndIdEmpresaAndEstadoTrue(id, idEmpresa).map(existing -> {
            existing.setEstado(false);
            return productoRepository.save(existing);
        });
    }
}
