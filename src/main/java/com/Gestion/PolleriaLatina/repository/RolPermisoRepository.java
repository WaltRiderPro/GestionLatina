package com.Gestion.PolleriaLatina.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Gestion.PolleriaLatina.model.Permiso;
import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.RolPermiso;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

  boolean existsByRolIdAndPermisoId(Long rolId, Long permisoId);

  boolean existsByRolAndPermiso(Rol rol, Permiso permiso);

  Optional<RolPermiso> findByRolAndPermiso(Rol rol, Permiso permiso);

}
