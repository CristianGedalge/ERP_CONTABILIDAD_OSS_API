package com.app.modulos.empresa.controllers;

import com.app.modulos.empresa.entities.Personal;
import com.app.modulos.empresa.services.PersonalService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
	private final PersonalService personalService;

	public PersonalController(PersonalService personalService) {
		this.personalService = personalService;
	}

	@GetMapping
	public ResponseEntity<List<Personal>> list() {
		return ResponseEntity.ok(personalService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Personal> get(@PathVariable Long id) {
		return personalService.findById(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Personal> create(@RequestBody Personal personal) {
		return ResponseEntity.ok(personalService.save(personal));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Personal> update(@PathVariable Long id, @RequestBody Personal personal) {
		return personalService.update(id, personal)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Personal> disable(@PathVariable Long id) {
		return personalService.disable(id)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
