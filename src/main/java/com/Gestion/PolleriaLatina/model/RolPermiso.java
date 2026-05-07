package com.Gestion.PolleriaLatina.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "rol_permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermiso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rol_id", nullable = false)
  @ToString.Exclude
  @JsonIgnore
  private Rol rol;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "permiso_id", nullable = false)
  private Permiso permiso;

  @Column(name = "puede_ver")
  @Builder.Default
  private boolean puedeVer = false;

  @Column(name = "puede_crear")
  @Builder.Default
  private boolean puedeCrear = false;

  @Column(name = "puede_editar")
  @Builder.Default
  private boolean puedeEditar = false;

  @Column(name = "puede_eliminar")
  @Builder.Default
  private boolean puedeEliminar = false;
}