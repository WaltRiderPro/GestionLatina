package com.Gestion.PolleriaLatina.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.Gestion.PolleriaLatina.dto.VentasResumenDTO;
import com.Gestion.PolleriaLatina.model.Venta;
import com.Gestion.PolleriaLatina.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

  private static final List<String> METODOS_PAGO = List.of("EFECTIVO", "YAPE", "PLIN", "TARJETA");

  private final VentaRepository ventaRepository;

  @Override
  @Transactional(readOnly = true)
  public VentasResumenDTO construirResumen(String q, LocalDate desde, LocalDate hasta, String metodoPago) {
    List<Venta> ventas = ventaRepository.findAllConPedidoCompletoOrderByFechaEmisionDesc();

    String busqueda = q != null ? q.trim().toUpperCase() : "";
    String metodoNormalizado = metodoPago != null ? metodoPago.trim().toUpperCase() : "";

    List<Venta> filtradas = ventas.stream()
        .filter(venta -> coincideBusqueda(venta, busqueda))
        .filter(venta -> coincideMetodoPago(venta, metodoNormalizado))
        .filter(venta -> coincideFecha(venta, desde, hasta))
        .collect(Collectors.toList());

    double montoTotal = filtradas.stream().mapToDouble(v -> valueOrZero(v.getMontoTotal())).sum();
    long totalVentas = filtradas.size();
    double ticketPromedio = totalVentas > 0 ? montoTotal / totalVentas : 0.0;

    LocalDate hoy = LocalDate.now();
    LocalDate inicioMes = hoy.withDayOfMonth(1);
    double montoHoy = ventas.stream()
        .filter(v -> fecha(v).isEqual(hoy))
        .mapToDouble(v -> valueOrZero(v.getMontoTotal()))
        .sum();
    long ventasHoy = ventas.stream().filter(v -> fecha(v).isEqual(hoy)).count();

    double montoMes = ventas.stream()
        .filter(v -> !fecha(v).isBefore(inicioMes))
        .mapToDouble(v -> valueOrZero(v.getMontoTotal()))
        .sum();
    long ventasMes = ventas.stream()
        .filter(v -> !fecha(v).isBefore(inicioMes))
        .count();

    Map<String, Long> ventasPorMetodo = new LinkedHashMap<>();
    METODOS_PAGO.forEach(metodo -> ventasPorMetodo.put(metodo, 0L));
    filtradas.forEach(venta -> {
      String metodo = normalize(venta.getMetodoPago());
      ventasPorMetodo.put(metodo, ventasPorMetodo.getOrDefault(metodo, 0L) + 1L);
    });
    ventasPorMetodo.entrySet().removeIf(entry -> entry.getValue() == 0L);

    return VentasResumenDTO.builder()
        .ventas(filtradas)
        .totalVentas(totalVentas)
        .montoTotal(montoTotal)
        .ventasHoy(ventasHoy)
        .montoHoy(montoHoy)
        .ventasMes(ventasMes)
        .montoMes(montoMes)
        .ticketPromedio(ticketPromedio)
        .ventasPorMetodoPago(ventasPorMetodo)
        .filtroTexto(q != null ? q : "")
        .filtroMetodoPago(metodoPago != null ? metodoPago : "")
        .filtroDesde(desde != null ? desde.toString() : "")
        .filtroHasta(hasta != null ? hasta.toString() : "")
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Venta obtenerDetalleVenta(Long id) {
    return ventaRepository.findConPedidoCompletoById(id)
        .orElseThrow(() -> new RuntimeException("La venta solicitada no existe."));
  }

  private boolean coincideBusqueda(Venta venta, String busqueda) {
    if (!StringUtils.hasText(busqueda)) {
      return true;
    }

    String comprobante = normalize(venta.getNumeroComprobante());
    String tipo = normalize(venta.getTipoComprobante());
    String cliente = venta.getPedido() != null ? normalize(venta.getPedido().getNombreCliente()) : "";
    String cajero = venta.getCajero() != null
        ? normalize(venta.getCajero().getNombre() + " " + venta.getCajero().getApellidos())
        : "";
    String mesa = venta.getPedido() != null && venta.getPedido().getMesa() != null
        ? normalize("mesa " + venta.getPedido().getMesa().getNumero())
        : "";

    return comprobante.contains(busqueda)
        || tipo.contains(busqueda)
        || cliente.contains(busqueda)
        || cajero.contains(busqueda)
        || mesa.contains(busqueda);
  }

  private boolean coincideMetodoPago(Venta venta, String metodoNormalizado) {
    return !StringUtils.hasText(metodoNormalizado) || "TODOS".equals(metodoNormalizado)
        || normalize(venta.getMetodoPago()).equals(metodoNormalizado);
  }

  private boolean coincideFecha(Venta venta, LocalDate desde, LocalDate hasta) {
    LocalDate fecha = fecha(venta);
    if (desde != null && fecha.isBefore(desde)) {
      return false;
    }
    if (hasta != null && fecha.isAfter(hasta)) {
      return false;
    }
    return true;
  }

  private LocalDate fecha(Venta venta) {
    LocalDateTime fechaEmision = venta.getFechaEmision() != null ? venta.getFechaEmision() : LocalDateTime.now();
    return fechaEmision.toLocalDate();
  }

  private double valueOrZero(Double value) {
    return value != null ? value : 0.0;
  }

  private String normalize(String text) {
    return text == null ? "" : text.trim().toUpperCase();
  }
}
