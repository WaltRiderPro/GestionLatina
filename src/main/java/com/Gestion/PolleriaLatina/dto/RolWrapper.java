package com.Gestion.PolleriaLatina.dto;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolWrapper {
  private Rol rol;
  private List<ModuloPermisoDto> permisos;
}