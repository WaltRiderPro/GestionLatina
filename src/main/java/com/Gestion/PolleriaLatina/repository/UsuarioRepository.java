package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

  Optional<Usuario> findByUsername(String username);

  Optional<Usuario> findByCorreo(String correo);

  Optional<Usuario> findByDni(String dni);

  @Query("SELECT u FROM Usuario u " +
      "WHERE u.rol.nombre = 'REPARTIDOR' " +
      "AND u.activo = true " +
      "AND u.eliminado = false " +
      "ORDER BY u.nombre ASC")
  List<Usuario> findRepartidoresActivos();

  Optional<Usuario> findByUsernameAndDniAndCorreoAndEliminadoFalse(String username, String dni, String correo);

  Optional<Usuario> findByResetTokenAndEliminadoFalse(String resetToken);
}