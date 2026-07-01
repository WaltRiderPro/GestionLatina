package com.Gestion.PolleriaLatina.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "incidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidencia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String titulo;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String descripcion;

  @Column(nullable = false, length = 50)
  private String tipo;

  @Column(nullable = false, length = 20)
  private String nivelGravedad;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_reporta_id", nullable = false)
  private Usuario usuarioReporta;

  @Column(columnDefinition = "TEXT")
  private String evidenciasJson;

  @Builder.Default
  @Column(nullable = false, length = 30)
  private String estado = "PENDIENTE";

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime fechaReporte;

  private LocalDateTime fechaResolucion;

  @Column(columnDefinition = "TEXT")
  private String respuestaAdmin;
}