package com.app.modulos.empresa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "empresa")
public class Empresa {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "razon_social", length = 200)
	private String razonSocial;

	@Column(name = "nit", length = 50)
	private String nit;

	@Column(name = "direccion", length = 200)
	private String direccion;

	@Column(name = "telefono", length = 50)
	private String telefono;

	@Column(name = "correo", length = 150)
	private String correo;

	@Column(name = "estado", nullable = false)
	private Boolean estado = true;

	@OneToMany(mappedBy = "empresa")
	private Set<com.app.modulos.saas.entities.Suscripcion> suscripciones = new HashSet<>();

	@OneToOne(mappedBy = "empresa")
	private com.app.modulos.saas.entities.Configuracion configuracion;

	@OneToMany(mappedBy = "empresa")
	@JsonIgnore
	private Set<com.app.modulos.usuario.entities.Usuario> usuarios = new HashSet<>();

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

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getNit() {
		return nit;
	}

	public void setNit(String nit) {
		this.nit = nit;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Set<com.app.modulos.saas.entities.Suscripcion> getSuscripciones() {
		return suscripciones;
	}

	public void setSuscripciones(Set<com.app.modulos.saas.entities.Suscripcion> suscripciones) {
		this.suscripciones = suscripciones;
	}

	public com.app.modulos.saas.entities.Configuracion getConfiguracion() {
		return configuracion;
	}

	public void setConfiguracion(com.app.modulos.saas.entities.Configuracion configuracion) {
		this.configuracion = configuracion;
	}

	public Set<com.app.modulos.usuario.entities.Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Set<com.app.modulos.usuario.entities.Usuario> usuarios) {
		this.usuarios = usuarios;
	}
}
