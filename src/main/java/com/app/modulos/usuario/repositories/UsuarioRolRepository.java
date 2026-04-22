package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.UsuarioRol;
import com.app.modulos.usuario.entities.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {
}
