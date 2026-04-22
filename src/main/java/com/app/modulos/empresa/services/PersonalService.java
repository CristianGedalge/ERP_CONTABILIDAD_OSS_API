package com.app.modulos.empresa.services;

import com.app.modulos.empresa.entities.Personal;
import com.app.modulos.empresa.repositories.PersonalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PersonalService {
	private final PersonalRepository personalRepository;

	public PersonalService(PersonalRepository personalRepository) {
		this.personalRepository = personalRepository;
	}

	public List<Personal> findAll() {
		return personalRepository.findAll();
	}

	public Optional<Personal> findById(Long id) {
		return personalRepository.findById(id);
	}

	public Personal save(Personal personal) {
		return personalRepository.save(personal);
	}

	public Optional<Personal> update(Long id, Personal input) {
		return personalRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setCi(input.getCi());
			existing.setCargo(input.getCargo());
			existing.setTelefono(input.getTelefono());
			existing.setCorreo(input.getCorreo());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			existing.setIdEmpresa(input.getIdEmpresa());
			return personalRepository.save(existing);
		});
	}

	public Optional<Personal> disable(Long id) {
		return personalRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			return personalRepository.save(existing);
		});
	}
}
