package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.BackupMetadata;
import com.app.modulos.usuario.repositories.BackupMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BackupService {
    private final BackupMetadataRepository backupMetadataRepository;

    public BackupService(BackupMetadataRepository backupMetadataRepository) {
        this.backupMetadataRepository = backupMetadataRepository;
    }

    public List<BackupMetadata> findAll() {
        return backupMetadataRepository.findAllByOrderByFechaCreacionDesc();
    }

    public List<BackupMetadata> findByEmpresaId(Long idEmpresa) {
        return backupMetadataRepository.findByIdEmpresaOrderByFechaCreacionDesc(idEmpresa);
    }

    public java.util.Optional<BackupMetadata> findById(Long id) {
        return backupMetadataRepository.findById(id);
    }

    @Transactional
    public BackupMetadata save(BackupMetadata backup) {
        return backupMetadataRepository.save(backup);
    }
}
