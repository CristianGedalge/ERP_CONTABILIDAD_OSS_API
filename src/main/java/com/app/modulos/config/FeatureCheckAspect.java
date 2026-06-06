package com.app.modulos.config;

import com.app.modulos.saas.entities.Suscripcion;
import com.app.modulos.saas.services.SuscripcionService;
import com.app.modulos.usuario.security.UserPrincipal;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Aspect
@Component
public class FeatureCheckAspect {
    private final SuscripcionService suscripcionService;

    public FeatureCheckAspect(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @Before("@within(requiresFeature) || @annotation(requiresFeature)")
    public void checkFeature(RequiresFeature requiresFeature) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        // SUPERADMIN has bypass
        if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            return;
        }

        Long empresaId = principal.getEmpresaId();
        if (empresaId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene empresa asignada.");
        }

        String feature = requiresFeature.value().toLowerCase().trim();

        Suscripcion sub = suscripcionService.findActiveByEmpresa(empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "No tiene una suscripción activa para su empresa."));

        boolean hasAccess = false;
        if (sub.getPlan() != null) {
            // First check explicit characteristics
            if (sub.getPlan().getCaracteristicas() != null) {
                hasAccess = sub.getPlan().getCaracteristicas().stream()
                        .filter(c -> c.getClave().toLowerCase().trim().equals(feature))
                        .map(c -> c.getValor().toLowerCase().trim())
                        .map(v -> v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("si"))
                        .findFirst()
                        .orElse(false);
            }

            // If not explicitly defined, check fallbacks matching frontend logic
            if (sub.getPlan().getCaracteristicas() == null || sub.getPlan().getCaracteristicas().stream().noneMatch(c -> c.getClave().toLowerCase().trim().equals(feature))) {
                String planName = sub.getPlan().getNombre().toLowerCase();
                
                if (isCoreModule(feature)) {
                    hasAccess = true;
                } else if (planName.contains("premium") || planName.contains("enterprise") || planName.contains("empresarial")) {
                    hasAccess = true;
                } else if (planName.contains("profesional") || planName.contains("pro")) {
                    hasAccess = !feature.equals("inventario");
                } else if (planName.contains("free") || planName.contains("gratis") || planName.contains("gratuito")) {
                    hasAccess = feature.equals("ventas") || feature.equals("compras");
                }
            }
        }

        if (!hasAccess) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Su plan de suscripción no incluye el módulo: " + requiresFeature.value());
        }
    }

    private boolean isCoreModule(String feature) {
        return java.util.List.of("mi-empresa", "suscripcion", "panel-control", "configuraciones", "roles-permisos", "empleados").contains(feature);
    }
}
