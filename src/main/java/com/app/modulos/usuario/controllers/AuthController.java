package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.dto.AuthRequest;
import com.app.modulos.usuario.dto.AuthResponse;
import com.app.modulos.usuario.dto.RegisterEmpresaRequest;
import com.app.modulos.usuario.dto.RegisterRequest;
import com.app.modulos.usuario.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.modulos.config.Auditable;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Auditable(accion = "LOGIN", modulo = "ACCESOS")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		try {
			return ResponseEntity.ok(authService.login(request));
		} catch (AuthenticationException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/register-empresa")
	@Auditable(accion = "REGISTRO_EMPRESA", modulo = "ACCESOS")
	public ResponseEntity<?> registerEmpresa(@RequestBody RegisterEmpresaRequest request) {
		return ResponseEntity.ok(authService.registerEmpresa(request));
	}

	@PostMapping("/logout")
	@Auditable(accion = "LOGOUT", modulo = "ACCESOS")
	public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
		try {
			authService.logout(token);
			return ResponseEntity.ok("Sesión cerrada exitosamente");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al cerrar sesión: " + e.getMessage());
		}
	}
}
