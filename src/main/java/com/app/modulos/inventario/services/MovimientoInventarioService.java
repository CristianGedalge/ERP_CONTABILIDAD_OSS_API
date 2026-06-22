package com.app.modulos.inventario.services;

import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.TipoMovimientoInventario;
import com.app.modulos.inventario.entities.TipoProducto;
import com.app.modulos.inventario.repositories.MovimientoInventarioRepository;
import com.app.modulos.inventario.repositories.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoInventarioService {
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoInventarioService(
        MovimientoInventarioRepository movimientoRepository,
        ProductoRepository productoRepository
    ) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    public List<MovimientoInventario> findAllByEmpresa(Long idEmpresa) {
        return movimientoRepository.findByIdEmpresaOrderByFechaDesc(idEmpresa);
    }

    public List<MovimientoInventario> findByProducto(Long productoId, Long idEmpresa) {
        return movimientoRepository.findByProductoIdAndIdEmpresaOrderByFechaDesc(productoId, idEmpresa);
    }

    @Transactional
    public MovimientoInventario registrarMovimiento(MovimientoInventario movimiento, Long idEmpresa) {
        if (movimiento.getProducto() == null || movimiento.getProducto().getId() == null) {
            throw new IllegalArgumentException("Debe especificar un producto válido.");
        }

        // 1. Obtener producto y validar pertenencia
        Producto producto = productoRepository.findByIdAndIdEmpresaAndEstadoTrue(
            movimiento.getProducto().getId(), 
            idEmpresa
        ).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado o no pertenece a tu empresa."));

        if (movimiento.getCantidad() == null || movimiento.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        // 2. Aplicar lógica de stock basándose en el tipo de movimiento
        BigDecimal cantidad = movimiento.getCantidad();
        BigDecimal stockActual = producto.getStockActual();

        if (movimiento.getTipo() == TipoMovimientoInventario.ENTRADA) {
            producto.setStockActual(stockActual.add(cantidad));
            // Actualizar costo unitario si viene en el movimiento
            if (movimiento.getCostoUnitario() != null && movimiento.getCostoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                producto.setCostoUnitario(movimiento.getCostoUnitario());
            }
        } else if (movimiento.getTipo() == TipoMovimientoInventario.SALIDA) {
            if (producto.getTipo() == TipoProducto.PRODUCTO && stockActual.compareTo(cantidad) < 0) {
                throw new IllegalArgumentException("Stock insuficiente para realizar esta salida. Stock actual: " + stockActual);
            }
            producto.setStockActual(stockActual.subtract(cantidad));
        } else if (movimiento.getTipo() == TipoMovimientoInventario.AJUSTE) {
            // Un ajuste puede ser positivo (entrada) o negativo (salida)
            // Se asume que la cantidad enviada puede representar el delta.
            // Para soportar ajustes negativos, podemos permitir que el movimiento tenga cantidad negativa,
            // o que la cantidad sea positiva y se reste si el flujo lo indica. 
            // Para mantener consistencia simple, si es ajuste sumamos el delta del movimiento directamente.
            BigDecimal nuevoStock = stockActual.add(cantidad);
            if (producto.getTipo() == TipoProducto.PRODUCTO && nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El ajuste resulta en un stock negativo no permitido. Stock actual: " + stockActual);
            }
            producto.setStockActual(nuevoStock);
        } else {
            throw new IllegalArgumentException("Tipo de movimiento desconocido.");
        }

        // 3. Guardar el producto con stock actualizado
        productoRepository.save(producto);

        // 4. Completar y guardar el movimiento de inventario
        movimiento.setProducto(producto);
        movimiento.setIdEmpresa(idEmpresa);
        if (movimiento.getFecha() == null) {
            movimiento.setFecha(LocalDateTime.now());
        }
        if (movimiento.getCostoUnitario() == null || movimiento.getCostoUnitario().compareTo(BigDecimal.ZERO) == 0) {
            movimiento.setCostoUnitario(producto.getCostoUnitario());
        }

        return movimientoRepository.save(movimiento);
    }
}
