package com.Gestion.PolleriaLatina.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Gestion.PolleriaLatina.dto.PedidoRequestDTO;
import com.Gestion.PolleriaLatina.model.Categoria;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.repository.CategoriaRepository;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.service.PuntoVentaService;
import com.Gestion.PolleriaLatina.service.SalonService; // IMPORTANTE INYECTAR ESTE SERVICIO

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/punto-venta")
@RequiredArgsConstructor
public class PuntoVentaController {

  private final PuntoVentaService puntoVentaService;
  private final SalonService salonService; // USAMOS EL SERVICIO DEL SALÓN QUE YA CALCULA EL STOCK
  private final CategoriaRepository categoriaRepository;
  private final PedidoRepository pedidoRepository;

  @GetMapping
  @PreAuthorize("hasAuthority('PUNTO_VENTA_VER')")
  public String verPuntoVenta(Model model) {

    // USAMOS LA FUNCIÓN QUE EXPLOTA LA RECETA PARA SABER SI HAY STOCK
    List<Producto> productos = salonService.listarProductosDisponibles();
    List<Categoria> categorias = categoriaRepository.findAll();

    model.addAttribute("productos", productos);
    model.addAttribute("categorias", categorias);
    model.addAttribute("titulo", "Terminal Punto de Venta (POS)");

    return "modules/operaciones/punto-venta";
  }

  @PostMapping("/procesar")
  @PreAuthorize("hasAuthority('PUNTO_VENTA_CREAR')")
  @ResponseBody
  public ResponseEntity<?> procesarComandaCheckout(@RequestBody PedidoRequestDTO request, Authentication auth) {
    try {
      String usernameCajero = auth.getName();
      var pedidoGuardado = puntoVentaService.procesarNuevaComanda(request, usernameCajero);
      String mensaje = request.isRequiereCobroInmediato()
          ? ("LOCAL".equalsIgnoreCase(request.getModalidad()) && request.getPedidoId() != null
              ? "Cobro realizado y mesa liberada."
              : "Cobro realizado y venta registrada.")
          : "Mesa aperturada y enviada a cocina.";

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", mensaje,
          "pedidoId", pedidoGuardado.getId()));

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  @GetMapping("/api/mesa/{numeroMesa}")
  @PreAuthorize("hasAuthority('PUNTO_VENTA_VER')")
  @ResponseBody
  public ResponseEntity<?> buscarCuentaMesa(@PathVariable Integer numeroMesa) {
    try {
      // Usamos la consulta que devuelve una Lista para evadir el
      // NonUniqueResultException
      List<Pedido> pedidos = pedidoRepository.findActiveOrderByMesaNumero(numeroMesa);

      if (pedidos.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "No hay ninguna orden activa para la mesa #" + numeroMesa));
      }

      Pedido pedido = pedidos.get(0);

      List<Map<String, Object>> items = pedido.getDetalles().stream().map(d -> {
        Map<String, Object> itemMap = new java.util.HashMap<>();
        itemMap.put("productoId", d.getProducto().getId());
        itemMap.put("nombre", d.getProducto().getNombre());
        itemMap.put("precio", d.getProducto().getPrecio());
        itemMap.put("cantidad", d.getCantidad());
        itemMap.put("subtotal", d.getSubtotal());
        return itemMap;
      }).collect(Collectors.toList());

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "pedidoId", pedido.getId(),
          "nombreCliente", pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "",
          "total", pedido.getTotal(),
          "items", items));

    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of(
          "status", "error",
          "message", "Error interno al buscar la mesa: " + e.getMessage()));
    }
  }
}
