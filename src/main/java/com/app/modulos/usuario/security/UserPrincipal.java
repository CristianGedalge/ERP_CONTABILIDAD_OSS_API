package com.app.modulos.usuario.security;

import com.app.modulos.usuario.entities.Permiso;
import com.app.modulos.usuario.entities.Rol;
import com.app.modulos.usuario.entities.Usuario;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
	private final Usuario usuario;

	public UserPrincipal(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new HashSet<>();
		Rol rol = usuario.getRol();
		if (rol != null) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
			for (Permiso permiso : rol.getPermisos()) {
				authorities.add(new SimpleGrantedAuthority("PERM_" + permiso.getNombre()));
			}
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return usuario.getPassword();
	}

	@Override
	public String getUsername() {
		return usuario.getCorreo();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return Boolean.TRUE.equals(usuario.getEstado());
	}

	public Usuario getUsuario() {
		return usuario;
	}
}
