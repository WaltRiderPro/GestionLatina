package com.Gestion.PolleriaLatina.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.service.PedidoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

  private final PedidoService pedidoService;
  private final PedidoRepository pedidoRepository;

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

  @PostMapping("/marcar-entregado")
  @PreAuthorize("hasAuthority('PEDIDOS_EDITAR')")
  public String marcarPedidoParaLlevarComoEntregado(@RequestParam("pedidoId") Long pedidoId, RedirectAttributes flash) {
    try {
      Pedido pedido = pedidoRepository.findById(pedidoId)
          .orElseThrow(() -> new RuntimeException("El pedido no existe."));

      pedido.setEstado("ENTREGADO");
      pedido.setFechaCompletado(java.time.LocalDateTime.now());
      pedidoRepository.save(pedido);

      flash.addFlashAttribute("success", "El pedido de " + pedido.getNombreCliente() + " fue entregado con éxito.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Error al completar: " + e.getMessage());
    }
    return "redirect:/pedidos/monitoreo";
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PEDIDOS_VER')")
  public String verHistorial(Model model) {
    List<Pedido> pedidos = pedidoRepository.findAllByOrderByFechaRegistroDesc();
    model.addAttribute("pedidos", pedidos);
    model.addAttribute("titulo", "Historial General de Pedidos");
    return "modules/operaciones/pedidos";
  }

  @GetMapping("/api/{id}")
  @PreAuthorize("hasAuthority('PEDIDOS_VER')")
  @ResponseBody
  public ResponseEntity<?> obtenerDetallePedidoParaHistorial(@PathVariable Long id) {
    try {
      Optional<Pedido> pedidoOpt = pedidoRepository.findPedidoConDetallesById(id);
      if (pedidoOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Pedido no encontrado."));
      }

      Pedido p = pedidoOpt.get();

      List<Map<String, Object>> items = p.getDetalles().stream().map(d -> {
        Map<String, Object> itemMap = new java.util.HashMap<>();
        itemMap.put("producto", d.getProducto().getNombre());
        itemMap.put("precio", d.getProducto().getPrecio());
        itemMap.put("cantidad", d.getCantidad());
        itemMap.put("subtotal", d.getSubtotal());
        return itemMap;
      }).collect(java.util.stream.Collectors.toList());

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "id", p.getId(),
          "fecha", p.getFechaRegistro() != null ? p.getFechaRegistro().toString() : "",
          "cliente", p.getNombreCliente() != null ? p.getNombreCliente() : "N/A",
          "modalidad", p.getModalidad(),
          "estado", p.getEstado(),
          "notas", p.getNotasAdicionales() != null ? p.getNotasAdicionales() : "",
          "total", p.getTotal(),
          "items", items));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", e.getMessage()));
    }
  }
}