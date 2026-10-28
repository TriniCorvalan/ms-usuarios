package com.example.usuarios.service;

import java.util.List;

import com.example.usuarios.dto.LoginRequest;
import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.RegisterRequest;
import com.example.usuarios.model.Usuario;

public interface UsuarioService {
	LoginResponse login(LoginRequest request);
	Usuario register(RegisterRequest request);
	List<Usuario> getAllUsuarios();
	Usuario getUsuarioById(Long id);
	Usuario updateUsuario(Long id, Usuario cambios);
	void deleteUsuario(Long id);
}
