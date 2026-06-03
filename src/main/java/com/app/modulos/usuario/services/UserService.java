package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<Usuario> findAll() {
		return userRepository.findAll();
	}

	public List<Usuario> findAllByEmpresa(Long idEmpresa) {
		return userRepository.findByIdEmpresaAndEstadoTrue(idEmpresa);
	}

	public Optional<Usuario> findById(Long id) {
		return userRepository.findById(id);
	}

	public Optional<Usuario> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public Optional<Usuario> findByCorreo(String correo) {
		return userRepository.findByCorreo(correo);
	}

	public Usuario save(Usuario usuario) {
		if (usuario.getPassword() != null) {
			usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		}
		return userRepository.save(usuario);
	}

	public Optional<Usuario> update(Long id, Usuario input) {
		return userRepository.findById(id).map(existing -> {
			existing.setUsername(input.getUsername());
			existing.setCorreo(input.getCorreo());
			if (input.getPassword() != null && !input.getPassword().isBlank()) {
				existing.setPassword(passwordEncoder.encode(input.getPassword()));
			}
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			if (input.getIdEmpresa() != null) {
				existing.setIdEmpresa(input.getIdEmpresa());
			}
			return userRepository.save(existing);
		});
	}

	public Optional<Usuario> disable(Long id) {
		return userRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			existing.setFechaDelete(LocalDateTime.now());
			return userRepository.save(existing);
		});
	}
}
