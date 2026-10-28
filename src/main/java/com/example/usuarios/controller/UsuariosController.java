package com.example.usuarios.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.usuarios.model.Usuario;
import com.example.usuarios.security.UsuarioPrincipal;
import com.example.usuarios.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuariosController {

	@Autowired
	private UsuarioService usuarioService;

	@GetMapping
	public List<Usuario> getAllUsuarios() {
		return usuarioService.getAllUsuarios();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUsuarioById(@PathVariable Long id, Authentication authentication) {
		Usuario found = usuarioService.getUsuarioById(id);
		if (found == null) {
			return ResponseEntity.notFound().build();
		}
		if (!puedeAcceder(authentication, id)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "No autorizado para ver este usuario"));
		}
		return ResponseEntity.ok(found);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario,
			Authentication authentication) {
		if (!puedeAcceder(authentication, id)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "No autorizado para modificar este usuario"));
		}
		try {
			// Solo ADMIN puede cambiar rol
			if (!esAdmin(authentication) && usuario.getRol() != null) {
				usuario.setRol(null);
			}
			Usuario updated = usuarioService.updateUsuario(id, usuario);
			return ResponseEntity.ok(updated);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
		try {
			usuarioService.deleteUsuario(id);
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.body(Map.of("message", "Usuario eliminado"));
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
		}
	}

	private boolean puedeAcceder(Authentication authentication, Long id) {
		if (esAdmin(authentication)) {
			return true;
		}
		if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
			return principal.getId().equals(id);
		}
		return false;
	}

	private boolean esAdmin(Authentication authentication) {
		return authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}
}
