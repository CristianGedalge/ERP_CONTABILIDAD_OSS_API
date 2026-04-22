package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<Usuario> findAll() {
		return userRepository.findAll();
	}

	public Optional<Usuario> findById(Long id) {
		return userRepository.findById(id);
	}

	public Usuario save(Usuario usuario) {
		return userRepository.save(usuario);
	}

	public Optional<Usuario> update(Long id, Usuario input) {
		return userRepository.findById(id).map(existing -> {
			existing.setUsername(input.getUsername());
			existing.setCorreo(input.getCorreo());
			if (input.getPassword() != null) {
				existing.setPassword(input.getPassword());
			}
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			existing.setIdEmpresa(input.getIdEmpresa());
			return userRepository.save(existing);
		});
	}

	public Optional<Usuario> disable(Long id) {
		return userRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			return userRepository.save(existing);
		});
	}
}
