package com.app.modulos.usuario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final Key signingKey;
	private final long expirationMs;

	public JwtService(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration-ms}") long expirationMs
	) {
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		if (userDetails instanceof UserPrincipal) {
			return generateTokenWithClaims((UserPrincipal) userDetails, now, expiry);
		}
		return Jwts.builder()
			.setSubject(userDetails.getUsername())
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private String generateTokenWithClaims(UserPrincipal principal, Date now, Date expiry) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", principal.getUsuario().getUsername());
		if (principal.getUsuario().getRol() != null) {
			claims.put("roleId", principal.getUsuario().getRol().getId());
			claims.put("roleName", principal.getUsuario().getRol().getNombre());
			Set<String> permisos = principal.getUsuario().getRol().getPermisos().stream()
				.map(permiso -> permiso.getNombre())
				.collect(Collectors.toSet());
			claims.put("permisos", permisos);
		}
		claims.put("empresaId", principal.getUsuario().getIdEmpresa());
		return Jwts.builder()
			.setClaims(claims)
			.setSubject(principal.getUsername())
			.setIssuedAt(now)
			.setExpiration(expiry)
			.signWith(signingKey, SignatureAlgorithm.HS256)
			.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public <T> T extractClaim(String token, String claimName, Class<T> type) {
		return extractAllClaims(token).get(claimName, type);
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(signingKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}
}
