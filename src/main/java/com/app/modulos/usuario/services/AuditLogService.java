package com.app.modulos.usuario.services;

import com.app.modulos.usuario.entities.AuditLog;
import com.app.modulos.usuario.repositories.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> findByEmpresaId(Long empresaId) {
        return auditLogRepository.findByEmpresaIdOrderByFechaHoraDesc(empresaId);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByFechaHoraDesc();
    }

    @Async
    public void saveLog(AuditLog log) {
        try {
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Error al guardar log de auditoría de forma asíncrona: " + e.getMessage());
        }
    }
}
