package com.example.usuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "USUARIO")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOMBRE", nullable = false, length = 100)
	private String nombre;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 150)
	private String email;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(name = "PASSWORD", nullable = false, length = 100)
	private String password;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ROL_ID", nullable = false)
	private Rol rol;

	@Column(name = "ACTIVO", nullable = false)
	private Boolean activo = true;

	@JsonIgnore
	public String getRolNombre() {
		return rol != null ? rol.getNombre() : null;
	}
}
