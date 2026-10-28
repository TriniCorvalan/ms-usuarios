package com.example.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.usuarios.model.Rol;
import com.example.usuarios.model.Usuario;
import com.example.usuarios.security.UsuarioPrincipal;
import com.example.usuarios.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuariosControllerTest {

	@Mock
	private UsuarioService usuarioService;

	@InjectMocks
	private UsuariosController controller;

	private MockMvc mockMvc;
	private Usuario usuario;
	private UsernamePasswordAuthenticationToken adminAuth;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		Rol admin = new Rol();
		admin.setId(1L);
		admin.setNombre("ADMIN");

		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNombre("Admin");
		usuario.setEmail("admin@foro.cl");
		usuario.setPassword("hash");
		usuario.setRol(admin);
		usuario.setActivo(true);

		UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
		adminAuth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
	}

	@Test
	void getAllUsuariosReturnsList() throws Exception {
		when(usuarioService.getAllUsuarios()).thenReturn(List.of(usuario));

		mockMvc.perform(get("/usuarios").principal(adminAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("admin@foro.cl"));
	}

	@Test
	void getUsuarioByIdWhenFoundReturnsOk() throws Exception {
		when(usuarioService.getUsuarioById(1L)).thenReturn(usuario);

		mockMvc.perform(get("/usuarios/1").principal(adminAuth))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void getUsuarioByIdWhenMissingReturnsNotFound() throws Exception {
		when(usuarioService.getUsuarioById(99L)).thenReturn(null);

		mockMvc.perform(get("/usuarios/99").principal(adminAuth))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateUsuarioReturnsOk() throws Exception {
		when(usuarioService.updateUsuario(eq(1L), any(Usuario.class))).thenReturn(usuario);

		mockMvc.perform(put("/usuarios/1")
						.principal(adminAuth)
						.contentType(APPLICATION_JSON)
						.content("{\"nombre\":\"Admin Actualizado\"}"))
				.andExpect(status().isOk());

		verify(usuarioService).updateUsuario(eq(1L), any(Usuario.class));
	}

	@Test
	void deleteUsuarioReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/usuarios/1").principal(adminAuth))
				.andExpect(status().isNoContent());

		verify(usuarioService).deleteUsuario(1L);
	}

	@Test
	void deleteUsuarioWhenMissingReturnsNotFound() throws Exception {
		doThrow(new RuntimeException("Usuario no encontrado")).when(usuarioService).deleteUsuario(1L);

		mockMvc.perform(delete("/usuarios/1").principal(adminAuth))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("Usuario no encontrado"));
	}
}
