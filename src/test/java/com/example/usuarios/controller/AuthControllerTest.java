package com.example.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.model.Rol;
import com.example.usuarios.model.Usuario;
import com.example.usuarios.security.JwtAuthenticationFilter;
import com.example.usuarios.security.SecurityConfig;
import com.example.usuarios.service.UsuarioService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = AuthController.class,
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				SecurityConfig.class,
				JwtAuthenticationFilter.class
		}))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void loginReturnsToken() throws Exception {
		Usuario usuario = new Usuario();
		usuario.setId(1L);
		usuario.setEmail("admin@foro.cl");
		Rol rol = new Rol();
		rol.setNombre("ADMIN");
		usuario.setRol(rol);

		when(usuarioService.login(any())).thenReturn(new LoginResponse("jwt-token", "Bearer", usuario));

		mockMvc.perform(post("/auth/login")
						.contentType(APPLICATION_JSON)
						.content("{\"email\":\"admin@foro.cl\",\"password\":\"Admin123!\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwt-token"))
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.usuario.email").value("admin@foro.cl"));
	}

	@Test
	void loginInvalidReturnsUnauthorized() throws Exception {
		when(usuarioService.login(any())).thenThrow(new RuntimeException("bad credentials"));

		mockMvc.perform(post("/auth/login")
						.contentType(APPLICATION_JSON)
						.content("{\"email\":\"x\",\"password\":\"y\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("Credenciales inválidas"));
	}

	@Test
	void registerReturnsCreated() throws Exception {
		Usuario usuario = new Usuario();
		usuario.setId(5L);
		usuario.setEmail("nuevo@foro.cl");
		usuario.setNombre("Nuevo");
		when(usuarioService.register(any())).thenReturn(usuario);

		mockMvc.perform(post("/auth/register")
						.contentType(APPLICATION_JSON)
						.content(jsonMapper.writeValueAsString(
								java.util.Map.of("nombre", "Nuevo", "email", "nuevo@foro.cl", "password", "User123!"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("nuevo@foro.cl"));
	}
}
