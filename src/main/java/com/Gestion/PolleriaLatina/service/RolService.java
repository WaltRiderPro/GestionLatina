package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Rol;

public interface RolService {
  List<Rol> listarRolesActivos();

  Rol obtenerPorId(Long id);

  void guardarCompleto(Rol rol, List<com.Gestion.PolleriaLatina.dto.ModuloPermisoDto> permisosDto);

  void eliminarLogico(Long id);

  void cambiarEstadoActivo(Long id);
}