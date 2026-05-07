package com.Gestion.PolleriaLatina.model;

import java.time.LocalDateTime;

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
@Table(name = "movimientos_almacen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoAlmacen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insumo_id", nullable = false)
  private Insumo insumo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuarioResponsable;

  // "INGRESO" (Compras) o "SALIDA" (Consumo por ventas, Mermas, Vencimiento)
  @Column(nullable = false, length = 20)
  private String tipoMovimiento;

  @Column(nullable = false)
  private Double cantidad;

  @Column(columnDefinition = "TEXT")
  private String motivo; // Ej: "Compra a San Fernando factura #123"

  @Builder.Default
  @Column(nullable = false)
  private LocalDateTime fechaMovimiento = LocalDateTime.now();
}