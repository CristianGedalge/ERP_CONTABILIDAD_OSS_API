package com.app.modulos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class WebLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Ignorar logs de recursos estáticos si los hubiera para no ensuciar
        String uri = request.getRequestURI();
        if (uri.contains("/favicon.ico") || uri.contains("/swagger-ui") || uri.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            
            // Construir un mensaje profesional y visualmente fácil de leer
            StringBuilder log = new StringBuilder();
            log.append("\n");
            log.append("================================================================================\n");
            log.append(String.format("  [HTTP %s] %s\n", method, uri));
            log.append(String.format("  STATUS: %d %s\n", status, getStatusLabel(status)));
            log.append(String.format("  TIME  : %dms\n", duration));
            log.append("================================================================================");
            
            if (status >= 400) {
                System.err.println(log.toString());
            } else {
                System.out.println(log.toString());
            }
        }
    }

    private String getStatusLabel(int status) {
        if (status >= 200 && status < 300) return "✅ OK";
        if (status == 401) return "🔐 UNAUTHORIZED";
        if (status == 403) return "🚫 FORBIDDEN";
        if (status == 404) return "🔎 NOT FOUND";
        if (status >= 500) return "🔥 SERVER ERROR";
        return "❓";
    }
}
