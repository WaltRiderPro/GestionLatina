package com.Gestion.PolleriaLatina.service;

import com.Gestion.PolleriaLatina.dto.PedidoRequestDTO;
import com.Gestion.PolleriaLatina.model.Pedido;

public interface PuntoVentaService {

  Pedido procesarNuevaComanda(PedidoRequestDTO request, String usernameCajero);
}