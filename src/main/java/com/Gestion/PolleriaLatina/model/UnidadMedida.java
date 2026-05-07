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
@Table(name = "unidades_medida")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE unidades_medida SET eliminado = true WHERE id=?")
@SQLRestriction("eliminado = false")
public class UnidadMedida {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nombre;

  @Column(nullable = false, length = 10)
  private String abreviatura; // Ej: "Kg", "L", "Und"

  @Builder.Default
  @Column(nullable = false)
  private boolean activo = true;

  @Builder.Default
  @Column(nullable = false)
  private boolean eliminado = false;
}