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

@Entity
@Table(name = "configuracion")
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

	@Column(name = "odoo_url")
	private String odooUrl;

	@Column(name = "odoo_db")
	private String odooDb;

	@Column(name = "odoo_user")
	private String odooUser;

	@Column(name = "odoo_password")
	private String odooPassword;

	@Column(name = "odoo_company_id")
	private Integer odooCompanyId;

	@OneToOne
	@JoinColumn(name = "id_empresa", insertable = false, updatable = false)
	@JsonIgnore
	private Empresa empresa;

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

	public String getOdooUrl() {
		return odooUrl;
	}

	public void setOdooUrl(String odooUrl) {
		this.odooUrl = odooUrl;
	}

	public String getOdooDb() {
		return odooDb;
	}

	public void setOdooDb(String odooDb) {
		this.odooDb = odooDb;
	}

	public String getOdooUser() {
		return odooUser;
	}

	public void setOdooUser(String odooUser) {
		this.odooUser = odooUser;
	}

	public String getOdooPassword() {
		return odooPassword;
	}

	public void setOdooPassword(String odooPassword) {
		this.odooPassword = odooPassword;
	}

	public Integer getOdooCompanyId() {
		return odooCompanyId;
	}

	public void setOdooCompanyId(Integer odooCompanyId) {
		this.odooCompanyId = odooCompanyId;
	}
}
