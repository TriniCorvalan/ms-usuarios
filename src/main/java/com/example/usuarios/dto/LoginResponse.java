package com.example.usuarios.dto;

import com.example.usuarios.model.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private String token;
	private String tipo;
	private Usuario usuario;
}
