package com.app.modulos.empresa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

import org.hibernate.annotations.Filter;

@Entity
@Table(name = "configuracion")
@Filter(name = "tenantFilter", condition = "id_empresa = :empresaId")
public class Configuracion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "iva")
	private BigDecimal iva;

	@Column(name = "it")
	private BigDecimal it;

	@Column(name = "moneda", length = 50)
	private String moneda;

	@Column(name = "tipo_cambio")
	private BigDecimal tipoCambio;

	@Column(name = "estado", nullable = false)
	private Boolean estado = true;

	@Column(name = "id_empresa")
	private Long idEmpresa;

	@OneToOne
	@JoinColumn(name = "id_empresa", insertable = false, updatable = false)
	@JsonIgnore
	private Empresa empresa;

	@Column(name = "id_cuenta_caja")
	private Long idCuentaCaja;

	@Column(name = "id_cuenta_clientes")
	private Long idCuentaClientes;

	@Column(name = "id_cuenta_proveedores")
	private Long idCuentaProveedores;

	@Column(name = "id_cuenta_ventas")
	private Long idCuentaVentas;

	@Column(name = "id_cuenta_compras")
	private Long idCuentaCompras;

	@Column(name = "id_cuenta_iva_debito")
	private Long idCuentaIvaDebito;

	@Column(name = "id_cuenta_iva_credito")
	private Long idCuentaIvaCredito;

	@Column(name = "id_cuenta_it_gasto")
	private Long idCuentaItGasto;

	@Column(name = "id_cuenta_it_pasivo")
	private Long idCuentaItPasivo;

	@Column(name = "id_cuenta_inventario")
	private Long idCuentaInventario;

	@Column(name = "id_cuenta_costo_ventas")
	private Long idCuentaCostoVentas;

	@Column(name = "color_primario", length = 7)
	private String colorPrimario;

	@Column(name = "color_secundario", length = 7)
	private String colorSecundario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getIt() {
		return it;
	}

	public void setIt(BigDecimal it) {
		this.it = it;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public BigDecimal getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(BigDecimal tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Long getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(Long idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Long getIdCuentaCaja() {
		return idCuentaCaja;
	}

	public void setIdCuentaCaja(Long idCuentaCaja) {
		this.idCuentaCaja = idCuentaCaja;
	}

	public Long getIdCuentaClientes() {
		return idCuentaClientes;
	}

	public void setIdCuentaClientes(Long idCuentaClientes) {
		this.idCuentaClientes = idCuentaClientes;
	}

	public Long getIdCuentaProveedores() {
		return idCuentaProveedores;
	}

	public void setIdCuentaProveedores(Long idCuentaProveedores) {
		this.idCuentaProveedores = idCuentaProveedores;
	}

	public Long getIdCuentaVentas() {
		return idCuentaVentas;
	}

	public void setIdCuentaVentas(Long idCuentaVentas) {
		this.idCuentaVentas = idCuentaVentas;
	}

	public Long getIdCuentaCompras() {
		return idCuentaCompras;
	}

	public void setIdCuentaCompras(Long idCuentaCompras) {
		this.idCuentaCompras = idCuentaCompras;
	}

	public Long getIdCuentaIvaDebito() {
		return idCuentaIvaDebito;
	}

	public void setIdCuentaIvaDebito(Long idCuentaIvaDebito) {
		this.idCuentaIvaDebito = idCuentaIvaDebito;
	}

	public Long getIdCuentaIvaCredito() {
		return idCuentaIvaCredito;
	}

	public void setIdCuentaIvaCredito(Long idCuentaIvaCredito) {
		this.idCuentaIvaCredito = idCuentaIvaCredito;
	}

	public Long getIdCuentaItGasto() {
		return idCuentaItGasto;
	}

	public void setIdCuentaItGasto(Long idCuentaItGasto) {
		this.idCuentaItGasto = idCuentaItGasto;
	}

	public Long getIdCuentaItPasivo() {
		return idCuentaItPasivo;
	}

	public void setIdCuentaItPasivo(Long idCuentaItPasivo) {
		this.idCuentaItPasivo = idCuentaItPasivo;
	}

	public Long getIdCuentaInventario() {
		return idCuentaInventario;
	}

	public void setIdCuentaInventario(Long idCuentaInventario) {
		this.idCuentaInventario = idCuentaInventario;
	}

	public Long getIdCuentaCostoVentas() {
		return idCuentaCostoVentas;
	}

	public void setIdCuentaCostoVentas(Long idCuentaCostoVentas) {
		this.idCuentaCostoVentas = idCuentaCostoVentas;
	}

	public String getColorPrimario() {
		return colorPrimario;
	}

	public void setColorPrimario(String colorPrimario) {
		this.colorPrimario = colorPrimario;
	}

	public String getColorSecundario() {
		return colorSecundario;
	}

	public void setColorSecundario(String colorSecundario) {
		this.colorSecundario = colorSecundario;
	}
}
