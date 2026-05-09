package com.Gestion.PolleriaLatina.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public List<Usuario> listarUsuariosActivos() {
    return usuarioRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Usuario obtenerPorId(Long id) {
    return usuarioRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void guardarUsuario(Usuario usuario) {
    Optional<Usuario> existeUsername = usuarioRepository.findByUsername(usuario.getUsername());
    if (existeUsername.isPresent() && !existeUsername.get().getId().equals(usuario.getId())) {
      throw new RuntimeException("El nombre de usuario '" + usuario.getUsername() + "' ya está en uso.");
    }

    Optional<Usuario> existeDni = usuarioRepository.findByDni(usuario.getDni());
    if (existeDni.isPresent() && !existeDni.get().getId().equals(usuario.getId())) {
      throw new RuntimeException("El DNI '" + usuario.getDni() + "' ya está registrado en el sistema.");
    }

    Optional<Usuario> existeCorreo = usuarioRepository.findByCorreo(usuario.getCorreo());
    if (existeCorreo.isPresent() && !existeCorreo.get().getId().equals(usuario.getId())) {
      throw new RuntimeException("El correo '" + usuario.getCorreo() + "' ya está registrado.");
    }

    if (usuario.getId() == null) {
      usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    } else {
      Usuario usuarioAntiguo = obtenerPorId(usuario.getId());
      if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
        usuario.setPassword(usuarioAntiguo.getPassword());
      } else {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
      }
    }

    usuarioRepository.save(usuario);
  }

  @Override
  @Transactional
  public void eliminarLogico(Long id) {
    Usuario usuario = obtenerPorId(id);
    if (usuario != null) {
      long timestamp = System.currentTimeMillis();
      usuario.setUsername(usuario.getUsername() + "_DEL_" + timestamp);
      usuario.setDni(usuario.getDni() + "_DEL_" + String.valueOf(timestamp).substring(8));

      usuario.setCorreo(timestamp + "_DEL_" + usuario.getCorreo());

      usuario.setEliminado(true);
      usuario.setActivo(false);

      usuarioRepository.save(usuario);
    }
  }

  @Override
  @Transactional
  public void cambiarEstadoActivo(Long id) {
    Usuario usuario = obtenerPorId(id);
    if (usuario != null) {
      usuario.setActivo(!usuario.isActivo());
      usuarioRepository.save(usuario);
    }
  }
}