package com.app.modulos.usuario.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {
	@EmbeddedId
	private UsuarioRolId id;

	@ManyToOne
	@MapsId("idUsuario")
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;

	@ManyToOne
	@MapsId("idRol")
	@JoinColumn(name = "id_rol")
	private Rol rol;

	public UsuarioRolId getId() {
		return id;
	}

	public void setId(UsuarioRolId id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Rol getRol() {
		return rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}
}
