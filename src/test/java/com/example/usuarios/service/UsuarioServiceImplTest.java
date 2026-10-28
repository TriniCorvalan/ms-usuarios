package com.example.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.usuarios.dto.RegisterRequest;
import com.example.usuarios.model.Rol;
import com.example.usuarios.model.Usuario;
import com.example.usuarios.repository.RolRepository;
import com.example.usuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private RolRepository rolRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioServiceImpl service;

	private Rol rolUsuario;
	private Usuario usuario;

	@BeforeEach
	void setUp() {
		rolUsuario = new Rol();
		rolUsuario.setId(2L);
		rolUsuario.setNombre("USUARIO");

		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNombre("Juan");
		usuario.setEmail("juan@foro.cl");
		usuario.setPassword("hash");
		usuario.setRol(rolUsuario);
		usuario.setActivo(true);
	}

	@Test
	void getAllUsuariosReturnsList() {
		when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
		assertEquals(1, service.getAllUsuarios().size());
	}

	@Test
	void getUsuarioByIdWhenFound() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		assertEquals(usuario, service.getUsuarioById(1L));
	}

	@Test
	void getUsuarioByIdWhenMissingReturnsNull() {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
		assertEquals(null, service.getUsuarioById(99L));
	}

	@Test
	void registerCreatesUsuarioWithRoleUsuario() {
		RegisterRequest request = new RegisterRequest();
		request.setNombre("Ana");
		request.setEmail("ana@foro.cl");
		request.setPassword("User123!");

		when(usuarioRepository.existsByEmail("ana@foro.cl")).thenReturn(false);
		when(rolRepository.findByNombre("USUARIO")).thenReturn(Optional.of(rolUsuario));
		when(passwordEncoder.encode("User123!")).thenReturn("encoded");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
			Usuario u = inv.getArgument(0);
			u.setId(10L);
			return u;
		});

		Usuario created = service.register(request);
		assertEquals("ana@foro.cl", created.getEmail());
		assertEquals("encoded", created.getPassword());
		assertEquals("USUARIO", created.getRol().getNombre());
		verify(usuarioRepository).save(any(Usuario.class));
	}

	@Test
	void registerWhenEmailExistsThrows() {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("juan@foro.cl");
		when(usuarioRepository.existsByEmail("juan@foro.cl")).thenReturn(true);
		assertThrows(RuntimeException.class, () -> service.register(request));
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void deleteUsuarioWhenExists() {
		when(usuarioRepository.existsById(1L)).thenReturn(true);
		service.deleteUsuario(1L);
		verify(usuarioRepository).deleteById(1L);
	}

	@Test
	void deleteUsuarioWhenMissingThrows() {
		when(usuarioRepository.existsById(1L)).thenReturn(false);
		assertThrows(RuntimeException.class, () -> service.deleteUsuario(1L));
		verify(usuarioRepository, never()).deleteById(1L);
	}
}
