package com.example.usuarios.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.usuarios.dto.LoginRequest;
import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.RegisterRequest;
import com.example.usuarios.model.Rol;
import com.example.usuarios.model.Usuario;
import com.example.usuarios.repository.RolRepository;
import com.example.usuarios.repository.UsuarioRepository;
import com.example.usuarios.security.JwtService;
import com.example.usuarios.security.UsuarioPrincipal;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private RolRepository rolRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtService jwtService;

	@Override
	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
		String token = jwtService.generateToken(principal);
		Usuario usuario = usuarioRepository.findByEmail(principal.getEmail())
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		return new LoginResponse(token, "Bearer", usuario);
	}

	@Override
	public Usuario register(RegisterRequest request) {
		if (usuarioRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("El email ya está registrado");
		}
		Rol rolUsuario = rolRepository.findByNombre("USUARIO")
				.orElseThrow(() -> new RuntimeException("Rol USUARIO no configurado"));
		Usuario usuario = new Usuario();
		usuario.setNombre(request.getNombre());
		usuario.setEmail(request.getEmail());
		usuario.setPassword(passwordEncoder.encode(request.getPassword()));
		usuario.setRol(rolUsuario);
		usuario.setActivo(true);
		return usuarioRepository.save(usuario);
	}

	@Override
	public List<Usuario> getAllUsuarios() {
		return usuarioRepository.findAll();
	}

	@Override
	public Usuario getUsuarioById(Long id) {
		return usuarioRepository.findById(id).orElse(null);
	}

	@Override
	public Usuario updateUsuario(Long id, Usuario cambios) {
		Usuario existing = usuarioRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
		if (cambios.getNombre() != null) {
			existing.setNombre(cambios.getNombre());
		}
		if (cambios.getEmail() != null) {
			existing.setEmail(cambios.getEmail());
		}
		if (cambios.getPassword() != null && !cambios.getPassword().isBlank()) {
			existing.setPassword(passwordEncoder.encode(cambios.getPassword()));
		}
		if (cambios.getActivo() != null) {
			existing.setActivo(cambios.getActivo());
		}
		if (cambios.getRol() != null && cambios.getRol().getId() != null) {
			Rol rol = rolRepository.findById(cambios.getRol().getId())
					.orElseThrow(() -> new RuntimeException("Rol no encontrado"));
			existing.setRol(rol);
		}
		return usuarioRepository.save(existing);
	}

	@Override
	public void deleteUsuario(Long id) {
		if (!usuarioRepository.existsById(id)) {
			throw new RuntimeException("Usuario no encontrado");
		}
		usuarioRepository.deleteById(id);
	}
}
