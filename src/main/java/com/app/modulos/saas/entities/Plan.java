package com.app.modulos.saas.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "plan")
public class Plan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(columnDefinition = "TEXT")
	private String descripcion;

	@Column(nullable = false)
	private BigDecimal precio;

	@Column(name = "duracion_dias", nullable = false)
	private Integer duracionDias;

	@Column(nullable = false)
	private Boolean estado = true;

	@CreationTimestamp
	@Column(name = "fecha_creacion", updatable = false)
	private LocalDateTime fechaCreacion;

	@UpdateTimestamp
	@Column(name = "fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	@OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CaracteristicaPlan> caracteristicas = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public Integer getDuracionDias() {
		return duracionDias;
	}

	public void setDuracionDias(Integer duracionDias) {
		this.duracionDias = duracionDias;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public List<CaracteristicaPlan> getCaracteristicas() {
		return caracteristicas;
	}

	public void setCaracteristicas(List<CaracteristicaPlan> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}
}
