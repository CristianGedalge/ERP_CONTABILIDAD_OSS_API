package com.app.modulos.config;

import com.app.modulos.usuario.entities.AuditLog;
import com.app.modulos.usuario.security.UserPrincipal;
import com.app.modulos.usuario.services.AuditLogService;
import com.app.modulos.usuario.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Aspect
@Component
public class AuditLogAspect {
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    public AuditLogAspect(AuditLogService auditLogService, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @Around("@annotation(com.app.modulos.config.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        AuditLog log = new AuditLog();
        log.setModulo(auditable.modulo());
        log.setAccion(auditable.accion());
        log.setFechaHora(now);

        // Extraer info de request HTTP (IP, User Agent)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        // Extraer info de seguridad (Usuario, Empresa)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            log.setUsuarioId(principal.getUsuario() != null ? principal.getUsuario().getId() : null);
            log.setUsuarioNombre(principal.getUsername());
            log.setEmpresaId(principal.getEmpresaId());
        } else {
            // Caso de login: extraer correo del argumento AuthRequest
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof com.app.modulos.usuario.dto.AuthRequest) {
                com.app.modulos.usuario.dto.AuthRequest authReq = (com.app.modulos.usuario.dto.AuthRequest) args[0];
                log.setUsuarioNombre(authReq.getCorreo());
            } else if (args.length > 0 && args[0] instanceof com.app.modulos.usuario.dto.RegisterEmpresaRequest) {
                com.app.modulos.usuario.dto.RegisterEmpresaRequest regReq = (com.app.modulos.usuario.dto.RegisterEmpresaRequest) args[0];
                log.setUsuarioNombre(regReq.getUsuarioCorreo());
            } else {
                log.setUsuarioNombre("ANONIMO");
            }
        }

        // Elaborar descripción inicial
        String targetName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.setDescripcion(String.format("Acción %s ejecutada en el módulo %s (%s.%s)", 
                auditable.accion(), auditable.modulo(), targetName, methodName));

        Object result;
        try {
            result = joinPoint.proceed();
            log.setResultado("EXITO");
            
            // Si es login o registro exitoso y no teníamos empresaId/usuarioId, lo resolvemos por correo
            if (log.getUsuarioId() == null && log.getUsuarioNombre() != null && log.getUsuarioNombre().contains("@")) {
                userRepository.findByCorreo(log.getUsuarioNombre()).ifPresent(u -> {
                    log.setUsuarioId(u.getId());
                    log.setEmpresaId(u.getIdEmpresa());
                });
            }
        } catch (Throwable throwable) {
            log.setResultado("ERROR");
            log.setDetallesError(throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
            throw throwable;
        } finally {
            // Guardar el log de forma asíncrona
            auditLogService.saveLog(log);
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
