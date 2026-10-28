package com.example.usuarios.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.usuarios.model.Rol;
import com.example.usuarios.service.RolService;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*")
public class RolesController {

	@Autowired
	private RolService rolService;

	@GetMapping
	public List<Rol> getAllRoles() {
		return rolService.getAllRoles();
	}
}
