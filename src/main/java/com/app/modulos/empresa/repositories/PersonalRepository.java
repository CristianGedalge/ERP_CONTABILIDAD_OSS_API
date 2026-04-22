package com.app.modulos.empresa.repositories;

import com.app.modulos.empresa.entities.Personal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalRepository extends JpaRepository<Personal, Long> {
}
