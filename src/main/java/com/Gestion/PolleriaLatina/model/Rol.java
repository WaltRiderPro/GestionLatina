package com.Gestion.PolleriaLatina.model;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE roles SET eliminado = true WHERE id=?")
@SQLRestriction("eliminado = false")
public class Rol {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nombre;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Builder.Default
  @Column(nullable = false)
  private boolean activo = true;

  @Builder.Default
  @Column(nullable = false)
  private boolean eliminado = false;

  @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RolPermiso> permisos;
}