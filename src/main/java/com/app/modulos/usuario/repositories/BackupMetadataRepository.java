package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.BackupMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BackupMetadataRepository extends JpaRepository<BackupMetadata, Long> {
    List<BackupMetadata> findByIdEmpresaOrderByFechaCreacionDesc(Long idEmpresa);
    List<BackupMetadata> findAllByOrderByFechaCreacionDesc();
}
