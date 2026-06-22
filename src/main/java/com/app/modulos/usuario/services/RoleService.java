package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Permiso;
import com.app.modulos.usuario.repositories.RoleRepository;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.usuario.repositories.PermisoRepository;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PermisoRepository permisoRepository;

	public RoleService(RoleRepository roleRepository, UserRepository userRepository, PermisoRepository permisoRepository) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.permisoRepository = permisoRepository;
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

	@Transactional
	public Rol save(Rol rol) {
		if (rol.getPermisos() != null) {
			Set<Permiso> managedPermisos = new HashSet<>();
			for (Permiso p : rol.getPermisos()) {
				if (p.getId() != null) {
					permisoRepository.findById(p.getId()).ifPresent(managedPermisos::add);
				}
			}
			rol.setPermisos(managedPermisos);
		}
		return roleRepository.save(rol);
	}

	@Transactional
	public Optional<Rol> update(Long id, Rol input) {
		return roleRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setDescripcion(input.getDescripcion());
			if (input.getPermisos() != null) {
				Set<Permiso> managedPermisos = new HashSet<>();
				for (Permiso p : input.getPermisos()) {
					if (p.getId() != null) {
						permisoRepository.findById(p.getId()).ifPresent(managedPermisos::add);
					}
				}
				existing.setPermisos(managedPermisos);
			}
			if (input.getIdEmpresa() != null) {
				existing.setIdEmpresa(input.getIdEmpresa());
			}
			if (input.getEstado() != null && Boolean.TRUE.equals(input.getEstado())) {
				existing.setEstado(true);
			}
			return roleRepository.save(existing);
		});
	}

	@Transactional
	public Optional<Rol> disable(Long id) {
		return roleRepository.findById(id).map(existing -> {
			if (userRepository.existsByRolIdAndEstadoTrue(id)) {
				throw new IllegalStateException("No se puede eliminar o desactivar el rol porque hay usuarios activos asignados a él.");
			}
			existing.setEstado(false);
			existing.setFechaDelete(LocalDateTime.now());
			return roleRepository.save(existing);
		});
	}
}
