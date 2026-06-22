package com.app.modulos.config;

import com.app.modulos.usuario.security.UserPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.app.modulos..*Service.*(..))")
    public void enableTenantFilter() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                Long empresaId = principal.getEmpresaId();
                Session session = entityManager.unwrap(Session.class);
                if (empresaId != null) {
                    session.enableFilter("tenantFilter").setParameter("empresaId", empresaId);
                } else {
                    session.disableFilter("tenantFilter");
                }
            }
        } catch (Exception e) {
            // Ignorar en contextos que no tienen sesión Hibernate activa
        }
    }
}
