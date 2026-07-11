package com.Gestion.PolleriaLatina.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Gestion.PolleriaLatina.dto.VentasResumenDTO;
import com.Gestion.PolleriaLatina.model.Venta;
import com.Gestion.PolleriaLatina.service.VentaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VentaController {

  private static final List<String> METODOS_PAGO = List.of("TODOS", "EFECTIVO", "YAPE", "PLIN", "TARJETA");
  private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("es", "PE"));
  private final VentaService ventaService;

  @GetMapping({"/ventas", "/Ventas"})
  @PreAuthorize("hasAuthority('PUNTO_VENTA_VER')")
  public String verVentas(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "metodoPago", required = false) String metodoPago,
      @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
      @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
      Model model) {

    VentasResumenDTO resumen = ventaService.construirResumen(q, desde, hasta, metodoPago);

    model.addAttribute("resumen", resumen);
    model.addAttribute("metodosPago", METODOS_PAGO);
    model.addAttribute("titulo", "Historial de Ventas");
    return "modules/operaciones/ventas";
  }

  @GetMapping("/ventas/api/{id}")
  @PreAuthorize("hasAuthority('PUNTO_VENTA_VER')")
  @ResponseBody
  public ResponseEntity<?> detalleVenta(@PathVariable Long id) {
    try {
      Venta venta = ventaService.obtenerDetalleVenta(id);
      List<Map<String, Object>> items = venta.getPedido().getDetalles().stream()
          .map(detalle -> Map.<String, Object>of(
              "nombre", detalle.getProducto().getNombre(),
              "cantidad", detalle.getCantidad(),
              "precio", detalle.getProducto().getPrecio(),
              "subtotal", detalle.getSubtotal()))
          .collect(Collectors.toList());

      Map<String, Object> respuesta = new LinkedHashMap<>();
      respuesta.put("status", "success");
      respuesta.put("id", venta.getId());
      respuesta.put("numeroComprobante", venta.getNumeroComprobante());
      respuesta.put("tipoComprobante", venta.getTipoComprobante());
      respuesta.put("fechaEmision", venta.getFechaEmision() != null ? venta.getFechaEmision().format(FECHA_FORMATO) : "");
      respuesta.put("metodoPago", venta.getMetodoPago());
      respuesta.put("montoTotal", venta.getMontoTotal());
      respuesta.put("documentoCliente", venta.getDocumentoCliente() != null ? venta.getDocumentoCliente() : "");
      respuesta.put("razonSocialCliente", venta.getRazonSocialCliente() != null ? venta.getRazonSocialCliente() : "");
      respuesta.put("cliente", venta.getPedido() != null ? venta.getPedido().getNombreCliente() : "");
      respuesta.put("modalidad", venta.getPedido() != null ? venta.getPedido().getModalidad() : "");
      respuesta.put("estadoPedido", venta.getPedido() != null ? venta.getPedido().getEstado() : "");
      respuesta.put("mesa", venta.getPedido() != null && venta.getPedido().getMesa() != null
          ? "Mesa #" + venta.getPedido().getMesa().getNumero()
          : "Sin mesa");
      respuesta.put("cajero", venta.getCajero() != null
          ? venta.getCajero().getNombre() + " " + venta.getCajero().getApellidos()
          : "");
      respuesta.put("items", items);
      respuesta.put("notas", venta.getPedido() != null && venta.getPedido().getNotasAdicionales() != null
          ? venta.getPedido().getNotasAdicionales()
          : "");

      return ResponseEntity.ok(respuesta);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }
}
