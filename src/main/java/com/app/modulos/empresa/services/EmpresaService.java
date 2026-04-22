package com.app.modulos.empresa.services;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EmpresaService {
	private final EmpresaRepository empresaRepository;
	private final UserRepository userRepository;

	public EmpresaService(EmpresaRepository empresaRepository, UserRepository userRepository) {
		this.empresaRepository = empresaRepository;
		this.userRepository = userRepository;
	}

	public List<Empresa> findAll() {
		return empresaRepository.findAll();
	}

	public Optional<Empresa> findById(Long id) {
		return empresaRepository.findById(id);
	}

	public Empresa save(Empresa empresa) {
		return empresaRepository.save(empresa);
	}

	public Optional<Usuario> assignEmpresaToUser(Long empresaId, String correo) {
		return userRepository.findByCorreo(correo).map(usuario -> {
			usuario.setIdEmpresa(empresaId);
			return userRepository.save(usuario);
		});
	}

	public Optional<Empresa> update(Long id, Empresa input) {
		return empresaRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setRazonSocial(input.getRazonSocial());
			existing.setNit(input.getNit());
			existing.setDireccion(input.getDireccion());
			existing.setTelefono(input.getTelefono());
			existing.setCorreo(input.getCorreo());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			return empresaRepository.save(existing);
		});
	}

	public Optional<Empresa> disable(Long id) {
		return empresaRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			return empresaRepository.save(existing);
		});
	}
}
