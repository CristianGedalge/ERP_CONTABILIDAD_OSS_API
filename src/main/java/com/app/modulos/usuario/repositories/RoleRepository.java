package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Rol, Long> {
	Optional<Rol> findByNombre(String nombre);
	boolean existsByNombre(String nombre);
}
