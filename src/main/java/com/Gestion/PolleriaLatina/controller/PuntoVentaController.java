package com.Gestion.PolleriaLatina.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Gestion.PolleriaLatina.dto.PedidoRequestDTO;
import com.Gestion.PolleriaLatina.model.Categoria;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.repository.CategoriaRepository;
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
      var nuevoPedido = puntoVentaService.procesarNuevaComanda(request, usernameCajero);
      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "Comanda procesada correctamente.",
          "pedidoId", nuevoPedido.getId(),
          "modalidad", nuevoPedido.getModalidad()));

    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }
}