package com.Gestion.PolleriaLatina.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.service.DeliveryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  @GetMapping("/pedidos")
  @PreAuthorize("hasAuthority('PEDIDOS_VER')")
  public String verGestionDelivery(Model model) {
    List<Pedido> pedidosDelivery = deliveryService.listarPedidosDelivery();
    model.addAttribute("pedidos", pedidosDelivery);
    model.addAttribute("titulo", "Gestión de Pedidos Delivery");
    return "modules/delivery/pedidos";
  }

  @GetMapping("/repartidores")
  @PreAuthorize("hasAuthority('PEDIDOS_VER')")
  public String verAsignacionRepartidores(Model model) {
    List<Pedido> pedidosListos = deliveryService.listarPedidosListosParaDespachar();
    List<Usuario> motorizadosActivos = deliveryService.listarRepartidoresActivos();

    model.addAttribute("pedidosListos", pedidosListos);
    model.addAttribute("repartidores", motorizadosActivos);
    model.addAttribute("titulo", "Asignación de Repartidores");
    return "modules/delivery/repartidores";
  }

  @PostMapping("/repartidores/asignar")
  @PreAuthorize("hasAuthority('PEDIDOS_EDITAR')")
  public String procesarAsignacion(
      @RequestParam("pedidoId") Long pedidoId,
      @RequestParam("repartidorId") Long repartidorId,
      @RequestParam("costoEnvio") Double costoEnvio,
      RedirectAttributes flash) {
    try {
      deliveryService.asignarDespacho(pedidoId, repartidorId, costoEnvio);
      flash.addFlashAttribute("success", "Pedido despachado con éxito. Se ha integrado a la ruta del motorizado.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/delivery/repartidores";
  }

  @GetMapping("/monitoreo")
  @PreAuthorize("hasAuthority('DELIVERY_VER')")
  public String verMonitoreoEnvios(Model model) {
    List<Pedido> pedidosEnRuta = deliveryService.listarPedidosEnTransito();
    model.addAttribute("pedidosEnRuta", pedidosEnRuta);
    model.addAttribute("titulo", "Monitoreo de Envíos en Tiempo Real");
    return "modules/delivery/monitoreo";
  }

  @GetMapping("/mis-despachos")
  @PreAuthorize("hasAuthority('DELIVERY_VER')")
  public String verMisDespachos(Model model, Authentication auth) {
    String usernameActual = auth.getName();
    List<Pedido> miRuta = deliveryService.listarRutaActivaMotorizado(usernameActual);

    model.addAttribute("miRuta", miRuta);
    model.addAttribute("titulo", "Mi Hoja de Ruta Activa");
    return "modules/delivery/mis-despachos";
  }

  @PostMapping("/mis-despachos/actualizar-estado")
  @PreAuthorize("hasAuthority('DELIVERY_EDITAR')")
  public String actualizarEstadoDespacho(
      @RequestParam("pedidoId") Long pedidoId,
      @RequestParam("nuevoEstado") String nuevoEstado,
      RedirectAttributes flash) {
    try {
      deliveryService.cambiarEstadoPedido(pedidoId, nuevoEstado);
      if ("ENTREGADO".equalsIgnoreCase(nuevoEstado)) {
        flash.addFlashAttribute("success", "¡Excelente! El pedido ha sido marcado como entregado con éxito.");
      } else {
        flash.addFlashAttribute("success", "Estado del despacho actualizado correctamente.");
      }
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/delivery/mis-despachos";
  }
}