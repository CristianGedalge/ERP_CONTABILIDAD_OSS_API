package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.InfoUsuario;
import com.app.modulos.usuario.repositories.InfoUsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InfoUsuarioService {
	private final InfoUsuarioRepository infoUsuarioRepository;

	public InfoUsuarioService(InfoUsuarioRepository infoUsuarioRepository) {
		this.infoUsuarioRepository = infoUsuarioRepository;
	}

	public List<InfoUsuario> findAll() {
		return infoUsuarioRepository.findAll();
	}

	public List<InfoUsuario> findAllByEmpresa(Long idEmpresa) {
		return infoUsuarioRepository.findByUsuarioIdEmpresa(idEmpresa);
	}

	public Optional<InfoUsuario> findById(Long id) {
		return infoUsuarioRepository.findById(id);
	}

	public Optional<InfoUsuario> findByUsuarioId(Long usuarioId) {
		return infoUsuarioRepository.findByUsuarioId(usuarioId);
	}

	public InfoUsuario save(InfoUsuario infoUsuario) {
		if (infoUsuario.getUsuario() != null && infoUsuario.getUsuario().getId() != null) {
			Optional<InfoUsuario> existing = infoUsuarioRepository.findByUsuarioId(infoUsuario.getUsuario().getId());
			if (existing.isPresent()) {
				// Si ya existe, actualizamos los datos en lugar de crear uno nuevo para evitar errores de duplicado
				InfoUsuario current = existing.get();
				current.setNombre(infoUsuario.getNombre());
				current.setCi(infoUsuario.getCi());
				current.setCargo(infoUsuario.getCargo());
				current.setTelefono(infoUsuario.getTelefono());
				return infoUsuarioRepository.save(current);
			}
		}
		return infoUsuarioRepository.save(infoUsuario);
	}

	public Optional<InfoUsuario> update(Long id, InfoUsuario input) {
		return infoUsuarioRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setCi(input.getCi());
			existing.setCargo(input.getCargo());
			existing.setTelefono(input.getTelefono());
			// No cambiamos el usuario en un update por ID
			return infoUsuarioRepository.save(existing);
		});
	}

	public Optional<InfoUsuario> updateByUsuarioId(Long usuarioId, InfoUsuario input) {
		return infoUsuarioRepository.findByUsuarioId(usuarioId).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setCi(input.getCi());
			existing.setCargo(input.getCargo());
			existing.setTelefono(input.getTelefono());
			return infoUsuarioRepository.save(existing);
		});
	}

	public Optional<InfoUsuario> delete(Long id) {
		return infoUsuarioRepository.findById(id).map(existing -> {
			infoUsuarioRepository.delete(existing);
			return existing;
		});
	}
}
