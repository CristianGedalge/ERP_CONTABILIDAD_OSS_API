package com.app.modulos.usuario.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", nullable = false, unique = true, length = 100)
	private String username;

	@Column(name = "correo", nullable = false, unique = true, length = 150)
	private String correo;

	@Column(name = "password", nullable = false)
	@com.fasterxml.jackson.annotation.JsonIgnore
	private String password;

	@Column(name = "estado", nullable = false)
	private Boolean estado = true;

	@Column(name = "id_empresa")
	private Long idEmpresa;

	@ManyToOne
	@JoinColumn(name = "id_empresa", insertable = false, updatable = false)
	@JsonIgnore
	private com.app.modulos.empresa.entities.Empresa empresa;

	@ManyToOne
	@JoinColumn(name = "id_rol")
	private Rol rol;

	@OneToOne(mappedBy = "usuario")
	private InfoUsuario infoUsuario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public com.app.modulos.empresa.entities.Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(com.app.modulos.empresa.entities.Empresa empresa) {
		this.empresa = empresa;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public InfoUsuario getInfoUsuario() {
		return infoUsuario;
	}

	public void setInfoUsuario(InfoUsuario infoUsuario) {
		this.infoUsuario = infoUsuario;
	}
}
