package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Receta;

public interface RecetaService {
  List<Receta> listarIngredientesPorProducto(Long productoId);

  Receta obtenerPorId(Long id);

  void guardarIngrediente(Receta receta);

  void eliminarIngrediente(Long id);
}