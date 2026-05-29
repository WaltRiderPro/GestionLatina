package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Usuario;

public interface DeliveryService {

  List<Pedido> listarPedidosDelivery();

  List<Pedido> listarPedidosListosParaDespachar();

  List<Pedido> listarRutaActivaMotorizado(String username);

  List<Pedido> listarPedidosEnTransito();

  List<Usuario> listarRepartidoresActivos();

  void asignarDespacho(Long pedidoId, Long repartidorId, Double costoEnvio);

  void cambiarEstadoPedido(Long pedidoId, String nuevoEstado);
}