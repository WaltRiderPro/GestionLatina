package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Usuario;

public interface UsuarioService {
  List<Usuario> listarUsuariosActivos();

  Usuario obtenerPorId(Long id);

  void guardarUsuario(Usuario usuario);

  void eliminarLogico(Long id);

  void cambiarEstadoActivo(Long id);
}