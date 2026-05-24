package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.UnidadMedida;

public interface UnidadMedidaService {
  List<UnidadMedida> listarActivos();

  List<UnidadMedida> listarTodas();

  UnidadMedida obtenerPorId(Long id);

  void guardar(UnidadMedida unidad);

  void eliminarLogico(Long id);

  void cambiarEstadoActivo(Long id);
}