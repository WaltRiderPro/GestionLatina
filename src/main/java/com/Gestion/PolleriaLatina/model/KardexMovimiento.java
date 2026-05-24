package com.Gestion.PolleriaLatina.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.Gestion.PolleriaLatina.model.enumerados.TipoMovimiento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "kardex_movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexMovimiento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "insumo_id", nullable = false)
  private Insumo insumo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TipoMovimiento tipoMovimiento;

  @Column(nullable = false)
  private Double cantidad;

  @Column(nullable = false)
  private Double stockResultante;

  @Column(nullable = false, length = 50)
  private String origen;

  @Column(columnDefinition = "TEXT")
  private String observacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime fechaRegistro;
}