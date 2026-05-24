package com.Gestion.PolleriaLatina.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Receta;
import com.Gestion.PolleriaLatina.repository.RecetaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecetaServiceImpl implements RecetaService {

  private final RecetaRepository recetaRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Receta> listarIngredientesPorProducto(Long productoId) {
    return recetaRepository.findByProductoIdCompleto(productoId);
  }

  @Override
  @Transactional(readOnly = true)
  public Receta obtenerPorId(Long id) {
    return recetaRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void guardarIngrediente(Receta receta) {
    Optional<Receta> existente = recetaRepository.findByProductoIdAndInsumoId(
        receta.getProducto().getId(),
        receta.getInsumo().getId());

    if (existente.isPresent() && !existente.get().getId().equals(receta.getId())) {
      throw new RuntimeException(
          "Este insumo ya se encuentra registrado en la receta de este producto. Modifique su cantidad si es necesario.");
    }

    if (receta.getCantidadNecesaria() <= 0) {
      throw new RuntimeException("La cantidad necesaria del ingrediente debe ser mayor a 0.");
    }

    recetaRepository.save(receta);
  }

  @Override
  @Transactional
  public void eliminarIngrediente(Long id) {
    if (!recetaRepository.existsById(id)) {
      throw new RuntimeException("El ingrediente que intenta remover no existe.");
    }
    recetaRepository.deleteById(id);
  }
}