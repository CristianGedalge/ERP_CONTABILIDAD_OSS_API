package com.app.modulos.contabilidad.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asiento_contable")
public class AsientoContable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha", nullable = false)
	private LocalDateTime fecha;

	@Column(name = "glosa", nullable = false, length = 255)
	private String glosa;

	@Column(name = "id_empresa", nullable = false)
	private Long idEmpresa;

	@Column(name = "origen_documento", length = 100)
	private String origenDocumento;

	@Column(name = "origen_id")
	private Long origenId;

	@OneToMany(mappedBy = "asiento", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetalleAsiento> detalles = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public String getGlosa() {
		return glosa;
	}

	public void setGlosa(String glosa) {
		this.glosa = glosa;
	}

	public Long getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(Long idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public String getOrigenDocumento() {
		return origenDocumento;
	}

	public void setOrigenDocumento(String origenDocumento) {
		this.origenDocumento = origenDocumento;
	}

	public Long getOrigenId() {
		return origenId;
	}

	public void setOrigenId(Long origenId) {
		this.origenId = origenId;
	}

	public List<DetalleAsiento> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<DetalleAsiento> detalles) {
		this.detalles = detalles;
	}

	public void addDetalle(DetalleAsiento detalle) {
		detalles.add(detalle);
		detalle.setAsiento(this);
	}
}
