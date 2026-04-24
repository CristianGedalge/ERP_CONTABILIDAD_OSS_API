package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);
	Optional<Usuario> findByCorreo(String correo);
	boolean existsByUsername(String username);
	boolean existsByCorreo(String correo);
	List<Usuario> findByIdEmpresaAndEstadoTrue(Long idEmpresa);
}
