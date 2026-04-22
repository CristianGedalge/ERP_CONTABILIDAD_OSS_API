package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.Permiso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
	Optional<Permiso> findByNombre(String nombre);
	boolean existsByNombre(String nombre);
}
