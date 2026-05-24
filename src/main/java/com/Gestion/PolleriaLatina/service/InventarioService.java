package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.InventarioCabecera;

public interface InventarioService {
  List<InventarioCabecera> listarHistorial();

  InventarioCabecera obtenerPorId(Long id);

  void procesarTomaInventario(InventarioCabecera inventario); 

  void procesarAjusteMasivo(String tipoMovimiento, String motivo, String referencia,
      List<Long> insumoIds, List<Double> cantidades, String username);
}