package com.Gestion.PolleriaLatina.service;

import java.time.LocalDate;

import com.Gestion.PolleriaLatina.dto.VentasResumenDTO;
import com.Gestion.PolleriaLatina.model.Venta;

public interface VentaService {

  VentasResumenDTO construirResumen(String q, LocalDate desde, LocalDate hasta, String metodoPago);

  Venta obtenerDetalleVenta(Long id);
}
