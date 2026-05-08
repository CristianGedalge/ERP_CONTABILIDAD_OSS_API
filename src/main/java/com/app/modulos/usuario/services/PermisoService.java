package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.Permiso;
import com.app.modulos.usuario.repositories.PermisoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PermisoService {
	private final PermisoRepository permisoRepository;

	public PermisoService(PermisoRepository permisoRepository) {
		this.permisoRepository = permisoRepository;
	}

	public List<Permiso> findAll() {
		return permisoRepository.findAll();
	}

	public Optional<Permiso> findById(Long id) {
		return permisoRepository.findById(id);
	}

	public Permiso save(Permiso permiso) {
		if (permisoRepository.existsByNombre(permiso.getNombre())) {
			return permisoRepository.findByNombre(permiso.getNombre()).get();
		}
		return permisoRepository.save(permiso);
	}

	public void delete(Long id) {
		permisoRepository.deleteById(id);
	}
}
