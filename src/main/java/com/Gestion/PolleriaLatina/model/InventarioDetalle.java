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
@Table(name = "inventario_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioDetalle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventario_cabecera_id", nullable = false)
  private InventarioCabecera inventarioCabecera;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insumo_id", nullable = false)
  private Insumo insumo;

  @Column(nullable = false)
  private Double stockTeorico;

  @Column(nullable = false)
  private Double stockFisico;

  @Column(nullable = false)
  private Double diferencia;
}