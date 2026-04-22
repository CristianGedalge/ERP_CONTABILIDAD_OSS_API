package com.app.modulos.empresa.repositories;

import com.app.modulos.empresa.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
