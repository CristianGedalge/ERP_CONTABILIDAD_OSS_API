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

	public Optional<InfoUsuario> findById(Long id) {
		return infoUsuarioRepository.findById(id);
	}

	public InfoUsuario save(InfoUsuario infoUsuario) {
		return infoUsuarioRepository.save(infoUsuario);
	}

	public Optional<InfoUsuario> update(Long id, InfoUsuario input) {
		return infoUsuarioRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setCi(input.getCi());
			existing.setCargo(input.getCargo());
			existing.setTelefono(input.getTelefono());
			existing.setUsuario(input.getUsuario());
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
