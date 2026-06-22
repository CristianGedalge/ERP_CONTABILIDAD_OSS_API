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

	public Permiso update(Long id, Permiso details) {
		Permiso p = permisoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Permiso no encontrado con el id: " + id));
		if (!p.getNombre().equals(details.getNombre()) && permisoRepository.existsByNombre(details.getNombre())) {
			throw new RuntimeException("Ya existe un permiso con el nombre: " + details.getNombre());
		}
		p.setNombre(details.getNombre());
		p.setDescripcion(details.getDescripcion());
		return permisoRepository.save(p);
	}

	public void delete(Long id) {
		permisoRepository.deleteById(id);
	}
}
