package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.InfoUsuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoUsuarioRepository extends JpaRepository<InfoUsuario, Long> {
	List<InfoUsuario> findByUsuarioIdEmpresa(Long idEmpresa);
}
