package com.Gestion.PolleriaLatina.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE mesas SET eliminado = true WHERE id=?")
@SQLRestriction("eliminado = false")
public class Mesa {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private Integer numero;

  @Column(nullable = false)
  private Integer capacidadPersonas;

  // Estados: "LIBRE", "OCUPADA", "MANTENIMIENTO"
  @Column(nullable = false, length = 20)
  @Builder.Default
  private String estado = "LIBRE";

  @Builder.Default
  @Column(nullable = false)
  private boolean activo = true;

  @Builder.Default
  @Column(nullable = false)
  private boolean eliminado = false;
}