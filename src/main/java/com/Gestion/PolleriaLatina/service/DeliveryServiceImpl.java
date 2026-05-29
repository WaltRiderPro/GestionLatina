package com.Gestion.PolleriaLatina.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final PedidoRepository pedidoRepository;
  private final UsuarioRepository usuarioRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosDelivery() {
    return pedidoRepository.findByModalidadDeliveryCompleto();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosListosParaDespachar() {
    return pedidoRepository.findPedidosListosParaDespachar();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarRutaActivaMotorizado(String username) {
    Usuario motorizado = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Motorizado no identificado en el sistema."));
    return pedidoRepository.findRutaActivaRepartidor(motorizado.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosEnTransito() {
    return pedidoRepository.findPedidosEnTransito();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Usuario> listarRepartidoresActivos() {
    return usuarioRepository.findRepartidoresActivos();
  }

  @Override
  @Transactional
  public void asignarDespacho(Long pedidoId, Long repartidorId, Double costoEnvio) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("El pedido solicitado no existe."));

    Usuario motorizado = usuarioRepository.findById(repartidorId)
        .orElseThrow(() -> new RuntimeException("El repartidor seleccionado no es válido o fue dado de baja."));

    if (!"LISTO".equalsIgnoreCase(pedido.getEstado())) {
      throw new RuntimeException("Operación inválida: El pedido debe estar en estado 'LISTO' para ser despachado.");
    }

    if (costoEnvio == null || costoEnvio < 0) {
      throw new RuntimeException("El costo de envío no puede ser un valor negativo.");
    }

    pedido.setRepartidor(motorizado);
    pedido.setCostoEnvio(costoEnvio);
    pedido.setEstado("EN_RUTA");

    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void cambiarEstadoPedido(Long pedidoId, String nuevoEstado) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("El pedido solicitado no existe."));

    String estadoUpper = nuevoEstado.toUpperCase();
    pedido.setEstado(estadoUpper);

    if ("ENTREGADO".equals(estadoUpper)) {
      pedido.setFechaCompletado(LocalDateTime.now());
    }

    pedidoRepository.save(pedido);
  }
}