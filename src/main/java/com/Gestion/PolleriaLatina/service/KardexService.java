package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.KardexMovimiento;

public interface KardexService {
  List<KardexMovimiento> obtenerHistorialCompleto();

  List<KardexMovimiento> obtenerHistorialPorInsumo(Long insumoId);
}