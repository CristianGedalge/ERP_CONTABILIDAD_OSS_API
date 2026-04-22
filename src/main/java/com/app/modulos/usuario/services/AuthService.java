package com.app.modulos.usuario.services;

import com.app.modulos.usuario.dto.AuthRequest;
import com.app.modulos.usuario.dto.AuthResponse;
import com.app.modulos.usuario.dto.RegisterRequest;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.usuario.security.JwtService;
import com.app.modulos.usuario.security.UserDetailsServiceImpl;
import java.util.HashSet;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;

	public AuthService(
		AuthenticationManager authenticationManager,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		UserDetailsServiceImpl userDetailsService
	) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	public AuthResponse login(AuthRequest request) {
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
		);
		UserDetails userDetails = userDetailsService.loadUserByUsername(request.getCorreo());
		String token = jwtService.generateToken(userDetails);
		return new AuthResponse(token);
	}

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new IllegalArgumentException("Username ya existe");
		}
		if (userRepository.existsByCorreo(request.getCorreo())) {
			throw new IllegalArgumentException("Correo ya existe");
		}

		Usuario usuario = new Usuario();
		usuario.setUsername(request.getUsername());
		usuario.setCorreo(request.getCorreo());
		usuario.setPassword(passwordEncoder.encode(request.getPassword()));
		usuario.setUsuarioRoles(new HashSet<>());
		userRepository.save(usuario);

		UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
		String token = jwtService.generateToken(userDetails);
		return new AuthResponse(token);
	}
}
