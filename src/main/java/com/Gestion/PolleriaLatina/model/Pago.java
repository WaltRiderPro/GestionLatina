package com.Gestion.PolleriaLatina.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relación 1 a 1 con el pedido
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pedido_id", nullable = false, unique = true)
  private Pedido pedido;

  // Ej: "EFECTIVO", "YAPE", "PLIN", "TARJETA"
  @Column(nullable = false, length = 50)
  private String metodoPago;

  @Column(nullable = false)
  private Double montoTotal;

  @Builder.Default
  @Column(nullable = false)
  private LocalDateTime fechaPago = LocalDateTime.now();

  // Ej: "PENDIENTE", "COMPLETADO", "ANULADO"
  @Column(nullable = false, length = 20)
  @Builder.Default
  private String estado = "PENDIENTE";
}