package com.Gestion.PolleriaLatina.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.RolRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {

    Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
      Rol nuevoRol = Rol.builder()
          .nombre("ADMIN")
          .descripcion("Administrador total del sistema")
          .activo(true)
          .eliminado(false)
          .build();
      return rolRepository.save(nuevoRol);
    });
    if (usuarioRepository.findByUsername("admin").isEmpty()) {
      Usuario admin = Usuario.builder()
          .username("admin")
          .password(passwordEncoder.encode("admin"))
          .nombre("Administrador")
          .apellidos("Pollería Latina")
          .dni("00000000")
          .correo("admin@pollerialatina.com")
          .rol(rolAdmin)
          .activo(true)
          .eliminado(false)
          .build();

      usuarioRepository.save(admin);
      System.out.println("Usuario 'admin' creado e insertado en la base de datos por Spring Boot.");
    }
  }
}