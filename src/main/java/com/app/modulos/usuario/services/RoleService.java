package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.repositories.RoleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
	private final RoleRepository roleRepository;

	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public List<Rol> findAll() {
		return roleRepository.findAll();
	}

	public Optional<Rol> findById(Long id) {
		return roleRepository.findById(id);
	}

	public Rol save(Rol rol) {
		return roleRepository.save(rol);
	}
}
