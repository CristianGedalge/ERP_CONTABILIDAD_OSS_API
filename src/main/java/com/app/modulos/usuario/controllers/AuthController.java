package com.app.modulos.usuario.controllers;

import com.app.modulos.usuario.dto.AuthRequest;
import com.app.modulos.usuario.dto.AuthResponse;
import com.app.modulos.usuario.dto.RegisterEmpresaRequest;
import com.app.modulos.usuario.dto.RegisterRequest;
import com.app.modulos.usuario.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/register-empresa")
	public ResponseEntity<AuthResponse> registerEmpresa(@RequestBody RegisterEmpresaRequest request) {
		return ResponseEntity.ok(authService.registerEmpresa(request));
	}
}
