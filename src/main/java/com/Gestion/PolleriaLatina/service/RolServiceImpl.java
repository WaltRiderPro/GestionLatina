package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.dto.ModuloPermisoDto;
import com.Gestion.PolleriaLatina.model.Permiso;
import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.RolPermiso;
import com.Gestion.PolleriaLatina.repository.PermisoRepository;
import com.Gestion.PolleriaLatina.repository.RolPermisoRepository;
import com.Gestion.PolleriaLatina.repository.RolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

  private final RolRepository rolRepository;
  private final PermisoRepository permisoRepository;
  private final RolPermisoRepository rolPermisoRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Rol> listarRolesActivos() {
    return rolRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Rol obtenerPorId(Long id) {
    return rolRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void guardarCompleto(Rol rol, List<ModuloPermisoDto> permisosDto) {
    Rol existente = rolRepository.findByNombre(rol.getNombre()).orElse(null);
    if (existente != null && !existente.getId().equals(rol.getId())) {
      throw new RuntimeException("Ya existe un rol activo con el nombre: " + rol.getNombre());
    }

    Rol rolGuardado = rolRepository.save(rol);

    if (permisosDto != null) {
      for (ModuloPermisoDto dto : permisosDto) {
        Permiso permiso = permisoRepository.findById(dto.getPermisoId())
            .orElseThrow(() -> new RuntimeException("Permiso no encontrado en BD"));

        RolPermiso rolPermiso = rolPermisoRepository.findByRolAndPermiso(rolGuardado, permiso)
            .orElse(RolPermiso.builder().rol(rolGuardado).permiso(permiso).build());

        rolPermiso.setPuedeVer(dto.isVer());
        rolPermiso.setPuedeCrear(dto.isCrear());
        rolPermiso.setPuedeEditar(dto.isEditar());
        rolPermiso.setPuedeEliminar(dto.isEliminar());

        rolPermisoRepository.save(rolPermiso);
      }
    }
  }

  @Override
  @Transactional
  public void eliminarLogico(Long id) {
    Rol rol = obtenerPorId(id);
    if (rol != null) {
      rol.setNombre(rol.getNombre() + "_ELIM_" + System.currentTimeMillis());
      rol.setEliminado(true);
      rol.setActivo(false);
      rolRepository.save(rol);
    }
  }

  @Override
  @Transactional
  public void cambiarEstadoActivo(Long id) {
    Rol rol = obtenerPorId(id);
    if (rol != null) {
      rol.setActivo(!rol.isActivo());
      rolRepository.save(rol);
    }
  }
}