package com.app.modulos.usuario.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;
	private final com.app.modulos.usuario.repositories.TokenRevocadoRepository tokenRevocadoRepository;

	public JwtAuthenticationFilter(
			JwtService jwtService, 
			UserDetailsServiceImpl userDetailsService,
			com.app.modulos.usuario.repositories.TokenRevocadoRepository tokenRevocadoRepository
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.tokenRevocadoRepository = tokenRevocadoRepository;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(7);
		
		// VALIDACIÓN DE BLACKLIST: Si el token está revocado, no seguir
		if (tokenRevocadoRepository.existsByToken(token)) {
			filterChain.doFilter(request, response);
			return;
		}

		String username = jwtService.extractUsername(token);

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (jwtService.isTokenValid(token, userDetails)) {
				request.setAttribute("correo", username);
				Object empresaIdObj = jwtService.extractClaim(token, "empresaId", Object.class);
				Object roleIdObj = jwtService.extractClaim(token, "roleId", Object.class);
				
				if (empresaIdObj != null) {
					request.setAttribute("empresaId", Long.valueOf(empresaIdObj.toString()));
				}
				if (roleIdObj != null) {
					request.setAttribute("roleId", Long.valueOf(roleIdObj.toString()));
				}
				
				String roleName = jwtService.extractClaim(token, "roleName", String.class);
				String claimUsername = jwtService.extractClaim(token, "username", String.class);
				
				if (roleName != null) {
					request.setAttribute("roleName", roleName);
				}
				if (claimUsername != null) {
					request.setAttribute("username", claimUsername);
				}
				UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		filterChain.doFilter(request, response);
	}
}
