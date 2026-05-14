package com.app.modulos.saas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suscripcion")
public class Suscripcion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "plan_id", nullable = false)
	private Plan plan;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin", nullable = false)
	private LocalDate fechaFin;

	@Column(name = "estado", nullable = false)
	private Boolean estado = true;

	@Column(name = "monto", nullable = false)
	private BigDecimal monto;

	@Column(name = "monto_pagado", nullable = false)
	private BigDecimal montoPagado;

	@Column(name = "tipo_renovacion", length = 50)
	private String tipoRenovacion; // Ej: MENSUAL, ANUAL

	@Column(name = "id_empresa", nullable = false)
	private Long idEmpresa;

	@ManyToOne
	@JoinColumn(name = "id_empresa", insertable = false, updatable = false)
	private com.app.modulos.empresa.entities.Empresa empresa;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public BigDecimal getMontoPagado() {
		return montoPagado;
	}

	public void setMontoPagado(BigDecimal montoPagado) {
		this.montoPagado = montoPagado;
	}

	public String getTipoRenovacion() {
		return tipoRenovacion;
	}

	public void setTipoRenovacion(String tipoRenovacion) {
		this.tipoRenovacion = tipoRenovacion;
	}

	public Long getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(Long idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public com.app.modulos.empresa.entities.Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(com.app.modulos.empresa.entities.Empresa empresa) {
		this.empresa = empresa;
	}
}
