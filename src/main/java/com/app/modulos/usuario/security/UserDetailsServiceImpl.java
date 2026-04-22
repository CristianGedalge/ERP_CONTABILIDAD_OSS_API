package com.app.modulos.usuario.security;

import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = userRepository.findByCorreo(username)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
		return new UserPrincipal(usuario);
	}
}
