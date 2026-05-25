package com.Gestion.PolleriaLatina.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.repository.InsumoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

  private final InsumoRepository insumoRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Insumo> listarInsumosActivos() {
    return insumoRepository.findAllConUnidades();
  }

  @Override
  @Transactional(readOnly = true)
  public Insumo obtenerPorId(Long id) {
    return insumoRepository.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void guardarInsumo(Insumo insumo) {
    Optional<Insumo> existente = insumoRepository.findByNombre(insumo.getNombre());
    if (existente.isPresent() && !existente.get().getId().equals(insumo.getId())) {
      throw new RuntimeException("Ya existe un insumo registrado con el nombre: " + insumo.getNombre());
    }
    if (insumo.getId() == null) {
      insumo.setStockActual(0.0);
    } else {
      Insumo insumoAntiguo = insumoRepository.findById(insumo.getId()).orElse(null);
      if (insumoAntiguo != null) {
        insumo.setStockActual(insumoAntiguo.getStockActual());
      }
    }

    insumoRepository.save(insumo);
  }

  @Override
  @Transactional
  public void eliminarLogico(Long id) {
    Insumo insumo = obtenerPorId(id);
    if (insumo != null) {
      insumo.setNombre(insumo.getNombre() + "_ELIM_" + System.currentTimeMillis());
      insumo.setEliminado(true);
      insumo.setActivo(false);
      insumoRepository.save(insumo);
    }
  }

  @Override
  @Transactional
  public void cambiarEstadoActivo(Long id) {
    Insumo insumo = obtenerPorId(id);
    if (insumo != null) {
      insumo.setActivo(!insumo.isActivo());
      insumoRepository.save(insumo);
    }
  }
}