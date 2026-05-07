package com.Gestion.PolleriaLatina.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE insumos SET eliminado = true WHERE id=?")
@SQLRestriction("eliminado = false")
public class Insumo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String nombre; // Ej: "Pollo Entero Crudo", "Papa Canchan", "Aceite Vegetal"

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "unidad_medida_id", nullable = false)
  private UnidadMedida unidadMedida;

  @Column(nullable = false)
  @Builder.Default
  private Double stockActual = 0.0;

  // PARA ALERTAS
  @Column(nullable = false)
  @Builder.Default
  private Double stockMinimo = 0.0;

  @Builder.Default
  @Column(nullable = false)
  private boolean activo = true;

  @Builder.Default
  @Column(nullable = false)
  private boolean eliminado = false;
}