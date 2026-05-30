package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Pedido;

public interface PedidoService {
  List<Pedido> listarPedidosActivosPorModalidad(String modalidad);
}