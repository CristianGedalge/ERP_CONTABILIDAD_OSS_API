package com.app.modulos.usuario.services;

import com.app.modulos.usuario.dto.AuthRequest;
import com.app.modulos.usuario.dto.AuthResponse;
import com.app.modulos.usuario.dto.RegisterEmpresaRequest;
import com.app.modulos.usuario.dto.RegisterRequest;
import com.app.modulos.usuario.entities.InfoUsuario;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.InfoUsuarioRepository;
import com.app.modulos.usuario.repositories.RoleRepository;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.usuario.security.JwtService;
import com.app.modulos.usuario.security.UserDetailsServiceImpl;
import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final EmpresaRepository empresaRepository;
	private final InfoUsuarioRepository infoUsuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserDetailsServiceImpl userDetailsService;

	public AuthService(
		AuthenticationManager authenticationManager,
		UserRepository userRepository,
		RoleRepository roleRepository,
		EmpresaRepository empresaRepository,
		InfoUsuarioRepository infoUsuarioRepository,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		UserDetailsServiceImpl userDetailsService
	) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.empresaRepository = empresaRepository;
		this.infoUsuarioRepository = infoUsuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	public AuthResponse login(AuthRequest request) {
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
		);
		UserDetails userDetails = userDetailsService.loadUserByCorreo(request.getCorreo());
		String token = jwtService.generateToken(userDetails);
		return new AuthResponse(token);
	}

	@Transactional
	public AuthResponse registerEmpresa(RegisterEmpresaRequest request) {
		if (userRepository.existsByUsername(request.getUsuarioUsername())) {
			throw new IllegalArgumentException("Username ya existe");
		}
		if (userRepository.existsByCorreo(request.getUsuarioCorreo())) {
			throw new IllegalArgumentException("Correo ya existe");
		}

		Empresa empresa = new Empresa();
		empresa.setNombre(request.getEmpresaNombre());
		empresa.setRazonSocial(request.getEmpresaRazonSocial());
		empresa.setNit(request.getEmpresaNit());
		empresa.setDireccion(request.getEmpresaDireccion());
		empresa.setTelefono(request.getEmpresaTelefono());
		empresa.setCorreo(request.getEmpresaCorreo());
		empresa = empresaRepository.save(empresa);

		Rol rol = new Rol();
		rol.setNombre("ADMIN");
		rol.setDescripcion("Administrador");
		rol.setIdEmpresa(empresa.getId());
		rol = roleRepository.save(rol);

		Usuario usuario = new Usuario();
		usuario.setUsername(request.getUsuarioUsername());
		usuario.setCorreo(request.getUsuarioCorreo());
		usuario.setPassword(passwordEncoder.encode(request.getUsuarioPassword()));
		usuario.setIdEmpresa(empresa.getId());
		usuario.setRol(rol);
		usuario = userRepository.save(usuario);

		InfoUsuario infoUsuario = new InfoUsuario();
		infoUsuario.setNombre(request.getInfoNombre());
		infoUsuario.setCi(request.getInfoCi());
		infoUsuario.setCargo(request.getInfoCargo());
		infoUsuario.setTelefono(request.getInfoTelefono());
		infoUsuario.setUsuario(usuario);
		infoUsuarioRepository.save(infoUsuario);

		UserDetails userDetails = userDetailsService.loadUserByCorreo(usuario.getCorreo());
		String token = jwtService.generateToken(userDetails);
		return new AuthResponse(token);
	}

	@Transactional
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
		if (request.getRolId() != null) {
			Rol rol = roleRepository.findById(request.getRolId())
				.orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
			usuario.setRol(rol);
			usuario.setIdEmpresa(rol.getIdEmpresa());
		}
		usuario = userRepository.save(usuario);

		InfoUsuario infoUsuario = new InfoUsuario();
		infoUsuario.setNombre(request.getNombre());
		infoUsuario.setCi(request.getCi());
		infoUsuario.setCargo(request.getCargo());
		infoUsuario.setTelefono(request.getTelefono());
		infoUsuario.setUsuario(usuario);
		infoUsuarioRepository.save(infoUsuario);

		UserDetails userDetails = userDetailsService.loadUserByCorreo(usuario.getCorreo());
		String token = jwtService.generateToken(userDetails);
		return new AuthResponse(token);
	}
}
