package com.Gestion.PolleriaLatina.Scheduled;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignadorDeliveryAutomatico {

  private final PedidoRepository pedidoRepository;
  private final UsuarioRepository usuarioRepository;

  @Scheduled(fixedRate = 300000) // Cada 5 minutos
  @Transactional
  public void despacharPedidosAutomaticamente() {

    List<Pedido> pedidosHuerfanos = pedidoRepository.findDeliveriesSinRepartidor();

    if (pedidosHuerfanos.isEmpty()) {
      return;
    }
    List<Usuario> motorizadosActivos = usuarioRepository.findRepartidoresActivos();

    if (motorizadosActivos.isEmpty()) {
      System.out
          .println("ALERTA: Hay pedidos de delivery en cola, pero no hay ningún REPARTIDOR activo en el sistema.");
      return;
    }

    int indexMotorizado = 0;
    int asignacionesRealizadas = 0;

    for (Pedido pedido : pedidosHuerfanos) {
      Usuario asignado = motorizadosActivos.get(indexMotorizado);

      pedido.setRepartidor(asignado);
      if (pedido.getCostoEnvio() == null) {
        pedido.setCostoEnvio(5.0);
      }

      if ("LISTO".equals(pedido.getEstado())) {
        pedido.setEstado("EN_RUTA");
      }

      pedidoRepository.save(pedido);
      asignacionesRealizadas++;

      System.out
          .println("AUTO-DESPACHO: Orden #" + pedido.getId() + " asignada al motorizado " + asignado.getNombre());

      indexMotorizado++;
      if (indexMotorizado >= motorizadosActivos.size()) {
        indexMotorizado = 0;
      }
    }

    if (asignacionesRealizadas > 0) {
      System.out.println("Robot Despachador: Se asignaron " + asignacionesRealizadas + " pedidos con éxito.");
    }
  }
}