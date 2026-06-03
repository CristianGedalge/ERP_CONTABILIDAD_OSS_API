package com.app.modulos.usuario.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "info_usuario")
public class InfoUsuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "ci", length = 50)
	private String ci;

	@Column(name = "cargo", length = 100)
	private String cargo;

	@Column(name = "telefono", length = 50)
	private String telefono;

	@OneToOne
	@JoinColumn(name = "id_usuario", unique = true)
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private Usuario usuario;

	@CreationTimestamp
	@Column(name = "fecha_create", updatable = false)
	private LocalDateTime fechaCreate;

	@Column(name = "fecha_delete")
	private LocalDateTime fechaDelete;

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

	public String getCi() {
		return ci;
	}

	public void setCi(String ci) {
		this.ci = ci;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}



	public LocalDateTime getFechaCreate() {
		return fechaCreate;
	}

	public void setFechaCreate(LocalDateTime fechaCreate) {
		this.fechaCreate = fechaCreate;
	}

	public LocalDateTime getFechaDelete() {
		return fechaDelete;
	}

	public void setFechaDelete(LocalDateTime fechaDelete) {
		this.fechaDelete = fechaDelete;
	}
}
