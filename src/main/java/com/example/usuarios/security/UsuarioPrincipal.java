package com.example.usuarios.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.usuarios.model.Usuario;

import lombok.Getter;

@Getter
public class UsuarioPrincipal implements UserDetails {

	private final Long id;
	private final String email;
	private final String password;
	private final String rol;
	private final boolean activo;

	public UsuarioPrincipal(Usuario usuario) {
		this.id = usuario.getId();
		this.email = usuario.getEmail();
		this.password = usuario.getPassword();
		this.rol = usuario.getRolNombre();
		this.activo = Boolean.TRUE.equals(usuario.getActivo());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return activo;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return activo;
	}
}
