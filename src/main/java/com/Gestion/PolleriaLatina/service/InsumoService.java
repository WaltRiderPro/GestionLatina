package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Insumo;

public interface InsumoService {
  List<Insumo> listarInsumosActivos();

  Insumo obtenerPorId(Long id);

  void guardarInsumo(Insumo insumo);

  void eliminarLogico(Long id);

  void cambiarEstadoActivo(Long id);
}