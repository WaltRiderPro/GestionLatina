package com.Gestion.PolleriaLatina.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.UnidadMedida;
import com.Gestion.PolleriaLatina.repository.UnidadMedidaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

  private final UnidadMedidaRepository unidadMedidaRepository;

  @Override
  @Transactional(readOnly = true)
  public List<UnidadMedida> listarActivos() {
    return unidadMedidaRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UnidadMedida> listarTodas() {
    return unidadMedidaRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public UnidadMedida obtenerPorId(Long id) {
    return unidadMedidaRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void guardar(UnidadMedida unidad) {
    Optional<UnidadMedida> existeNom = unidadMedidaRepository.findByNombre(unidad.getNombre());
    if (existeNom.isPresent() && !existeNom.get().getId().equals(unidad.getId())) {
      throw new RuntimeException("La unidad de medida '" + unidad.getNombre() + "' ya existe.");
    }
    unidadMedidaRepository.save(unidad);
  }

  @Override
  @Transactional
  public void eliminarLogico(Long id) {
    UnidadMedida unidad = obtenerPorId(id);
    if (unidad != null) {
      long timestamp = System.currentTimeMillis();
      unidad.setNombre(unidad.getNombre() + "_ELIM_" + timestamp);
      unidad.setAbreviatura(unidad.getAbreviatura() + "_" + timestamp % 1000);
      unidad.setEliminado(true);
      unidad.setActivo(false);
      unidadMedidaRepository.save(unidad);
    }
  }

  @Override
  @Transactional
  public void cambiarEstadoActivo(Long id) {
    UnidadMedida unidad = obtenerPorId(id);
    if (unidad != null) {
      unidad.setActivo(!unidad.isActivo());
      unidadMedidaRepository.save(unidad);
    }
  }
}