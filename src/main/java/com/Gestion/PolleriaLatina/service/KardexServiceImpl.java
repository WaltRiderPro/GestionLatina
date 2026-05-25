package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.KardexMovimiento;
import com.Gestion.PolleriaLatina.repository.KardexMovimientoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements KardexService {

  private final KardexMovimientoRepository kardexMovimientoRepository;

  @Override
  @Transactional(readOnly = true)
  public List<KardexMovimiento> obtenerHistorialCompleto() {
    return kardexMovimientoRepository.findAllCompleto();
  }

  @Override
  @Transactional(readOnly = true)
  public List<KardexMovimiento> obtenerHistorialPorInsumo(Long insumoId) {
    return kardexMovimientoRepository.findByInsumoIdCompleto(insumoId);
  }
}