package com.app.modulos.usuario.dto;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.entities.InfoUsuario;
import com.app.modulos.inventario.entities.Producto;
import com.app.modulos.inventario.entities.MovimientoInventario;
import com.app.modulos.contabilidad.entities.CuentaContable;
import com.app.modulos.contabilidad.entities.CentroCosto;
import com.app.modulos.contabilidad.entities.PeriodoContable;
import com.app.modulos.contabilidad.entities.AsientoContable;
import com.app.modulos.operaciones.entities.FacturaVenta;
import com.app.modulos.operaciones.entities.FacturaCompra;
import com.app.modulos.operaciones.entities.CuentaPorCobrar;
import com.app.modulos.operaciones.entities.CuentaPorPagar;
import com.app.modulos.usuario.entities.AuditLog;

import java.util.List;

public class EmpresaBackupDTO {
    private Empresa empresa;
    private List<Configuracion> configuraciones;
    private List<Suscripcion> suscripciones;
    private List<Rol> roles;
    private List<Usuario> usuarios;
    private List<InfoUsuario> infoUsuarios;
    private List<Producto> productos;
    private List<MovimientoInventario> movimientosInventario;
    private List<CuentaContable> cuentasContables;
    private List<CentroCosto> centrosCosto;
    private List<PeriodoContable> periodosContables;
    private List<AsientoContable> asientosContables;
    private List<FacturaVenta> facturasVenta;
    private List<FacturaCompra> facturasCompra;
    private List<CuentaPorCobrar> cuentasPorCobrar;
    private List<CuentaPorPagar> cuentasPorPagar;
    private List<AuditLog> auditLogs;

    public EmpresaBackupDTO() {}

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public List<Configuracion> getConfiguraciones() { return configuraciones; }
    public void setConfiguraciones(List<Configuracion> configuraciones) { this.configuraciones = configuraciones; }

    public List<Suscripcion> getSuscripciones() { return suscripciones; }
    public void setSuscripciones(List<Suscripcion> suscripciones) { this.suscripciones = suscripciones; }

    public List<Rol> getRoles() { return roles; }
    public void setRoles(List<Rol> roles) { this.roles = roles; }

    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }

    public List<InfoUsuario> getInfoUsuarios() { return infoUsuarios; }
    public void setInfoUsuarios(List<InfoUsuario> infoUsuarios) { this.infoUsuarios = infoUsuarios; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public List<MovimientoInventario> getMovimientosInventario() { return movimientosInventario; }
    public void setMovimientosInventario(List<MovimientoInventario> movimientosInventario) { this.movimientosInventario = movimientosInventario; }

    public List<CuentaContable> getCuentasContables() { return cuentasContables; }
    public void setCuentasContables(List<CuentaContable> cuentasContables) { this.cuentasContables = cuentasContables; }

    public List<CentroCosto> getCentrosCosto() { return centrosCosto; }
    public void setCentrosCosto(List<CentroCosto> centrosCosto) { this.centrosCosto = centrosCosto; }

    public List<PeriodoContable> getPeriodosContables() { return periodosContables; }
    public void setPeriodosContables(List<PeriodoContable> periodosContables) { this.periodosContables = periodosContables; }

    public List<AsientoContable> getAsientosContables() { return asientosContables; }
    public void setAsientosContables(List<AsientoContable> asientosContables) { this.asientosContables = asientosContables; }

    public List<FacturaVenta> getFacturasVenta() { return facturasVenta; }
    public void setFacturasVenta(List<FacturaVenta> facturasVenta) { this.facturasVenta = facturasVenta; }

    public List<FacturaCompra> getFacturasCompra() { return facturasCompra; }
    public void setFacturasCompra(List<FacturaCompra> facturasCompra) { this.facturasCompra = facturasCompra; }

    public List<CuentaPorCobrar> getCuentasPorCobrar() { return cuentasPorCobrar; }
    public void setCuentasPorCobrar(List<CuentaPorCobrar> cuentasPorCobrar) { this.cuentasPorCobrar = cuentasPorCobrar; }

    public List<CuentaPorPagar> getCuentasPorPagar() { return cuentasPorPagar; }
    public void setCuentasPorPagar(List<CuentaPorPagar> cuentasPorPagar) { this.cuentasPorPagar = cuentasPorPagar; }

    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
}
