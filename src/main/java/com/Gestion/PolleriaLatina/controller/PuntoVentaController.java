package com.Gestion.PolleriaLatina.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.Gestion.PolleriaLatina.repository.ProductoRepository;
import com.Gestion.PolleriaLatina.service.PuntoVentaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/punto-venta")
@RequiredArgsConstructor
public class PuntoVentaController {

  private final PuntoVentaService puntoVentaService;
  private final ProductoRepository productoRepository;
  private final CategoriaRepository categoriaRepository;
  private final PedidoRepository pedidoRepository;

  @GetMapping
  @PreAuthorize("hasAuthority('PUNTO_VENTA_VER')")
  public String verPuntoVenta(Model model) {

    List<Producto> productos = productoRepository.findAllConCategoriasYPresentaciones();
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

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message",
          request.isRequiereCobroInmediato() ? "Cobro realizado y mesa liberada."
              : "Mesa aperturada y enviada a cocina.",
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
      Optional<Pedido> pedidoOpt = pedidoRepository.findPedidoActivoByMesa(numeroMesa);

      if (pedidoOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", "error",
            "message", "No hay ninguna orden activa para la mesa #" + numeroMesa));
      }

      Pedido pedido = pedidoOpt.get();
      List<Map<String, Object>> items = pedido.getDetalles().stream().map(d -> {
        Map<String, Object> itemMap = new java.util.HashMap<>();
        itemMap.put("productoId", d.getProducto().getId());
        itemMap.put("nombre", d.getProducto().getNombre());
        itemMap.put("precio", d.getProducto().getPrecio());
        itemMap.put("cantidad", d.getCantidad());
        itemMap.put("subtotal", d.getSubtotal());
        return itemMap;
      }).collect(Collectors.toList());

      // Devolvemos los datos listos para que el JavaScript reconstruya el carrito
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