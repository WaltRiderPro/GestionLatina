package com.Gestion.PolleriaLatina.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mesas/api")
@RequiredArgsConstructor
public class MesaRestController {

  private final PedidoRepository pedidoRepository;

  @GetMapping("/pedido-activo/{mesaId}")
  public Map<String, Object> obtenerPedidoActivoMesa(@PathVariable Long mesaId) {
    Map<String, Object> response = new HashMap<>();

    List<Pedido> pedidos = pedidoRepository.findActiveOrderByMesaId(mesaId);
    Pedido pedido = pedidos.isEmpty() ? null : pedidos.get(0);

    if (pedido != null) {
      response.put("id", pedido.getId());
      response.put("nombreCliente", pedido.getNombreCliente());
      response.put("total", pedido.getTotal());
      response.put("notas", pedido.getNotasAdicionales());

      List<Map<String, Object>> listaItems = new ArrayList<>();
      for (DetallePedido dp : pedido.getDetalles()) {
        Map<String, Object> item = new HashMap<>();
        item.put("productoNombre", dp.getProducto().getNombre());
        item.put("precio", dp.getProducto().getPrecio());
        item.put("cantidad", dp.getCantidad());
        item.put("subtotal", dp.getSubtotal());
        listaItems.add(item);
      }
      response.put("detalles", listaItems);
    }
    return response;
  }

}