package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.InfoUsuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoUsuarioRepository extends JpaRepository<InfoUsuario, Long> {
	List<InfoUsuario> findByUsuarioIdEmpresa(Long idEmpresa);
	Optional<InfoUsuario> findByUsuarioId(Long usuarioId);
}
