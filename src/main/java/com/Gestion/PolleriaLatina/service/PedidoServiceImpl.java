package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

  private final PedidoRepository pedidoRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosActivosPorModalidad(String modalidad) {
    return pedidoRepository.findPedidosActivosPorModalidad(modalidad);
  }
}