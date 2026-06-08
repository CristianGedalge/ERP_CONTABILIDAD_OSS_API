package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEmpresaIdOrderByFechaHoraDesc(Long empresaId);
    List<AuditLog> findAllByOrderByFechaHoraDesc();
}
