package com.Gestion.PolleriaLatina.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Permiso;
import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.RolPermiso;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.PermisoRepository;
import com.Gestion.PolleriaLatina.repository.RolPermisoRepository;
import com.Gestion.PolleriaLatina.repository.RolRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UsuarioRepository usuarioRepository;
  private final RolRepository rolRepository;
  private final PermisoRepository permisoRepository;
  private final RolPermisoRepository rolPermisoRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {

    List<String[]> modulosData = Arrays.asList(
        new String[] { "DASHBOARD", "Principal" },
        new String[] { "REPORTES", "Principal" },
        new String[] { "PUNTO_VENTA", "Operaciones" },
        new String[] { "COCINA", "Operaciones" },
        new String[] { "MESAS", "Operaciones" },
        new String[] { "PEDIDOS", "Operaciones" },
        new String[] { "CLIENTES", "Operaciones" },
        new String[] { "PRODUCTOS", "Catálogo" },
        new String[] { "CATEGORIAS", "Catálogo" },
        new String[] { "PRESENTACIONES", "Catálogo" },
        new String[] { "INSUMOS", "Almacén" },
        new String[] { "KARDEX", "Almacén" },
        new String[] { "RECETAS", "Almacén" },
        new String[] { "UNIDADES", "Almacén" },
        new String[] { "USUARIOS", "Seguridad" },
        new String[] { "ROLES", "Seguridad" });

    for (String[] data : modulosData) {
      if (permisoRepository.findByNombre(data[0]).isEmpty()) {
        Permiso p = Permiso.builder()
            .nombre(data[0])
            .modulo(data[1])
            .descripcion("Acceso al módulo de " + data[0])
            .build();
        permisoRepository.save(p);
      }
    }

    Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
      Rol nuevoRol = Rol.builder()
          .nombre("ADMIN")
          .descripcion("Administrador supremo. No se puede eliminar ni editar.")
          .activo(true)
          .eliminado(false)
          .build();
      return rolRepository.save(nuevoRol);
    });

    List<Permiso> todosLosPermisos = permisoRepository.findAll();
    for (Permiso permiso : todosLosPermisos) {
      boolean existeVinculo = rolPermisoRepository.existsByRolAndPermiso(rolAdmin, permiso);
      if (!existeVinculo) {
        RolPermiso rp = RolPermiso.builder()
            .rol(rolAdmin)
            .permiso(permiso)
            .puedeVer(true)
            .puedeCrear(true)
            .puedeEditar(true)
            .puedeEliminar(true)
            .build();
        rolPermisoRepository.save(rp);
      }
    }

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
      System.out.println("SEMILLA COMPLETADA: Módulos, Rol ADMIN Supremo y Usuario admin generados.");
    }
  }
}