package com.Gestion.PolleriaLatina.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuloPermisoDto {
  private Long permisoId;
  private String nombreModulo;
  private String agrupamiento; // Ej: Operaciones, Almacén

  private boolean ver;
  private boolean crear;
  private boolean editar;
  private boolean eliminar;
}