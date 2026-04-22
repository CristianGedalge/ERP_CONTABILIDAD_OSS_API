package com.app.modulos.usuario.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioRolId implements Serializable {
	@Column(name = "id_usuario")
	private Long idUsuario;

	@Column(name = "id_rol")
	private Long idRol;

	public UsuarioRolId() {
	}

	public UsuarioRolId(Long idUsuario, Long idRol) {
		this.idUsuario = idUsuario;
		this.idRol = idRol;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Long getIdRol() {
		return idRol;
	}

	public void setIdRol(Long idRol) {
		this.idRol = idRol;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		UsuarioRolId that = (UsuarioRolId) o;
		return Objects.equals(idUsuario, that.idUsuario) && Objects.equals(idRol, that.idRol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUsuario, idRol);
	}
}
