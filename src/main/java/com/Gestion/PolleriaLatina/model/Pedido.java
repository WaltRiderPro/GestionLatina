package com.Gestion.PolleriaLatina.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nombreCliente;

  // Modalidad: "LOCAL", "LLEVAR", "DELIVERY"
  @Column(nullable = false, length = 20)
  private String modalidad;

  // Estados: "REGISTRADO", "EN_PREPARACION", "LISTO", "EN_RUTA", "ENTREGADO"
  @Column(nullable = false, length = 20)
  @Builder.Default
  private String estado = "REGISTRADO";

  // Campo vital para el motor de priorización dinámico
  @Column(nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime fechaRegistro = LocalDateTime.now();

  private LocalDateTime fechaCompletado;

  @Column(columnDefinition = "TEXT")
  private String notasAdicionales;

  // SOLO SI ES LOCAL
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mesa_id")
  private Mesa mesa;

  // Para repartidor: SI ES QUE ESTA ES MODALIDAD DELIVERY

  @Column(length = 255)
  private String direccionEntrega;

  @Column(length = 255)
  private String referenciaEntrega;

  @Column(length = 20)
  private String telefonoContacto;

  @Column(name = "costo_envio")
  @Builder.Default
  private Double costoEnvio = 0.0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "repartidor_id")
  private Usuario repartidor;

  // Relación con el repartidor

  @Column(nullable = false)
  private Double total;

  @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
  private java.util.List<DetallePedido> detalles;
}