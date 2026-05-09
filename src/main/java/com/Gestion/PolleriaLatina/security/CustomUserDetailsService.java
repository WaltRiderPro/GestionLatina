package com.Gestion.PolleriaLatina.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.RolPermiso;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    if (!usuario.isActivo()) {
      throw new RuntimeException("El usuario está inactivo.");
    }

    List<GrantedAuthority> authorities = new ArrayList<>();

    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));

    if (usuario.getRol().getPermisos() != null) {
      for (RolPermiso rp : usuario.getRol().getPermisos()) {
        String modulo = rp.getPermiso().getNombre().toUpperCase();

        if (rp.isPuedeVer())
          authorities.add(new SimpleGrantedAuthority(modulo + "_VER"));
        if (rp.isPuedeCrear())
          authorities.add(new SimpleGrantedAuthority(modulo + "_CREAR"));
        if (rp.isPuedeEditar())
          authorities.add(new SimpleGrantedAuthority(modulo + "_EDITAR"));
        if (rp.isPuedeEliminar())
          authorities.add(new SimpleGrantedAuthority(modulo + "_ELIMINAR"));
      }
    }

    return new User(
        usuario.getUsername(),
        usuario.getPassword(),
        authorities);
  }
}