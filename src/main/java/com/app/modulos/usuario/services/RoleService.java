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

	public List<Rol> findAllByEmpresa(Long idEmpresa) {
		return roleRepository.findByIdEmpresaAndEstadoTrueOrderByIdAsc(idEmpresa);
	}

	public Optional<Rol> findById(Long id) {
		return roleRepository.findByIdAndEstadoTrue(id);
	}

	public Rol save(Rol rol) {
		return roleRepository.save(rol);
	}

	public Optional<Rol> update(Long id, Rol input) {
		return roleRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setDescripcion(input.getDescripcion());
			if (input.getIdEmpresa() != null) {
				existing.setIdEmpresa(input.getIdEmpresa());
			}
			if (input.getEstado() != null && Boolean.TRUE.equals(input.getEstado())) {
				existing.setEstado(true);
			}
			return roleRepository.save(existing);
		});
	}

	public Optional<Rol> disable(Long id) {
		return roleRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			return roleRepository.save(existing);
		});
	}
}
