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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relación 1 a 1: Una venta es el cierre financiero de un único pedido
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pedido_id", nullable = false, unique = true)
  private Pedido pedido;

  // Tipos: "TICKET", "BOLETA", "FACTURA"
  @Column(nullable = false, length = 20)
  private String tipoComprobante;

  // Generado automáticamente (Ej: B001-0000145)
  @Column(length = 20)
  private String numeroComprobante;

  // Datos Fiscales del Cliente (DNI o RUC)
  @Column(length = 20)
  private String documentoCliente;

  @Column(length = 100)
  private String razonSocialCliente;

  // Métodos: "EFECTIVO", "YAPE", "PLIN", "TARJETA"
  @Column(nullable = false, length = 20)
  private String metodoPago;

  // Monto exacto que ingresó a la caja (Incluye delivery si aplica)
  @Column(nullable = false)
  private Double montoTotal;

  @Column(nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime fechaEmision = LocalDateTime.now();

  // Auditoría: Quién estaba en la caja registradora en ese momento
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cajero_id", nullable = false)
  private Usuario cajero;
}