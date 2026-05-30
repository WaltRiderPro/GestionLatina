package com.Gestion.PolleriaLatina.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

  private final PedidoService pedidoService;

  @GetMapping("/monitoreo")
  @PreAuthorize("hasAuthority('PEDIDOS_VER')")
  public String verMonitoreoGeneral(Model model) {

    List<Pedido> pedidosLocal = pedidoService.listarPedidosActivosPorModalidad("LOCAL");
    List<Pedido> pedidosLlevar = pedidoService.listarPedidosActivosPorModalidad("LLEVAR");
    List<Pedido> pedidosDelivery = pedidoService.listarPedidosActivosPorModalidad("DELIVERY");

    model.addAttribute("pedidosLocal", pedidosLocal);
    model.addAttribute("pedidosLlevar", pedidosLlevar);
    model.addAttribute("pedidosDelivery", pedidosDelivery);

    return "modules/operaciones/monitoreo";
  }
}