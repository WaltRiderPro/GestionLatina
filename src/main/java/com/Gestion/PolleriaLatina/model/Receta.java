package com.Gestion.PolleriaLatina.model;

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
@Table(name = "recetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // El platillo que vendes (Ej: 1/4 de Pollo)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  // La materia prima que gastas (Ej: Pollo Entero Crudo)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insumo_id", nullable = false)
  private Insumo insumo;

  // Cuánto gastas de ese insumo para hacer 1 unidad de este producto (Ej: 0.25)
  @Column(nullable = false)
  private Double cantidadNecesaria;
}