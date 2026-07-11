package com.Gestion.PolleriaLatina.service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.dto.DashboardSummary;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.LowStockItem;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.MonthlyRevenuePoint;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.Overview;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.RecentIncident;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.RecentOrder;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.StatusPoint;
import com.Gestion.PolleriaLatina.dto.DashboardSummary.TopProduct;
import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Incidencia;
import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.model.Mesa;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.repository.IncidenciaRepository;
import com.Gestion.PolleriaLatina.repository.InsumoRepository;
import com.Gestion.PolleriaLatina.repository.MesaRepository;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.repository.ProductoRepository;
import com.Gestion.PolleriaLatina.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

  private static final Locale LOCALE = Locale.forLanguageTag("es-PE");
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", LOCALE);
  private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yy", LOCALE);
  private static final DateTimeFormatter MONTH_TITLE = DateTimeFormatter.ofPattern("MMMM yyyy", LOCALE);
  private static final Set<String> CLOSED_STATES = Set.of("PAGADO", "ENTREGADO");

  private final PedidoRepository pedidoRepository;
  private final ProductoRepository productoRepository;
  private final InsumoRepository insumoRepository;
  private final MesaRepository mesaRepository;
  private final IncidenciaRepository incidenciaRepository;
  private final VentaRepository ventaRepository;

  public DashboardSummary buildSummary() {
    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate nextMonthStart = monthStart.plusMonths(1);
    LocalDate prevMonthStart = monthStart.minusMonths(1);

    List<Pedido> pedidos = pedidoRepository.findAllConDetallesYMesaOrderByFechaRegistroDesc();
    List<Pedido> pedidosCerrados = pedidos.stream()
        .filter(this::isClosed)
        .toList();

    List<Producto> productosActivos = productoRepository.findActiveProductsWithRelations();
    List<Insumo> insumos = insumoRepository.findAllConUnidades();
    List<Mesa> mesas = mesaRepository.findByActivoTrueAndEliminadoFalseOrderByNumeroAsc();
    List<Incidencia> incidencias = incidenciaRepository.findAllWithUsuarioOrderByFechaDesc();

    double ingresosHoy = sumRevenue(pedidosCerrados, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    double ingresosMes = sumRevenue(pedidosCerrados, monthStart.atStartOfDay(), nextMonthStart.atStartOfDay());
    double ingresosMesAnterior = sumRevenue(pedidosCerrados, prevMonthStart.atStartOfDay(), monthStart.atStartOfDay());
    long pedidosHoy = countClosedBetween(pedidosCerrados, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    long pedidosMes = countClosedBetween(pedidosCerrados, monthStart.atStartOfDay(), nextMonthStart.atStartOfDay());

    long pedidosActivos = pedidos.stream().filter(p -> !isClosed(p)).count();
    long deliveryEnRuta = pedidos.stream()
        .filter(p -> "DELIVERY".equalsIgnoreCase(p.getModalidad()) && "EN_RUTA".equalsIgnoreCase(p.getEstado()))
        .count();
    long mesasOcupadas = mesas.stream().filter(m -> "OCUPADA".equalsIgnoreCase(m.getEstado())).count();
    long pendientesIncidencias = incidencias.stream()
        .filter(i -> "PENDIENTE".equalsIgnoreCase(i.getEstado()))
        .count();
    long comprobantesEmitidos = ventaRepository.count();
    long productosActivosCount = productosActivos.size();

    List<LowStockItem> lowStockItems = insumos.stream()
        .filter(i -> i.getStockActual() <= i.getStockMinimo())
        .sorted(Comparator.comparingDouble(this::stockGap))
        .limit(5)
        .map(this::toLowStockItem)
        .toList();

    String currentMonth = today.format(MONTH_TITLE);
    String generatedAt = LocalDateTime.now().format(DATE_TIME);
    String revenueGrowth = formatGrowth(ingresosMesAnterior, ingresosMes);
    String averageTicket = formatCurrency(pedidosCerrados.isEmpty() ? 0.0
        : pedidosCerrados.stream().mapToDouble(this::effectiveAmount).sum() / pedidosCerrados.size());

    Overview overview = new Overview(
        generatedAt,
        currentMonth,
        formatCurrency(ingresosHoy),
        formatCurrency(ingresosMes),
        revenueGrowth,
        pedidosHoy,
        pedidosMes,
        pedidosActivos,
        deliveryEnRuta,
        mesasOcupadas,
        productosActivosCount,
        lowStockItems.size(),
        pendientesIncidencias,
        comprobantesEmitidos,
        averageTicket);

    return new DashboardSummary(
        overview,
        buildMonthlyRevenue(pedidosCerrados, today),
        buildOrderStatus(pedidos),
        buildTopProducts(pedidos),
        buildRecentOrders(pedidos),
        lowStockItems,
        buildRecentIncidents(incidencias));
  }

  private List<MonthlyRevenuePoint> buildMonthlyRevenue(List<Pedido> pedidosCerrados, LocalDate today) {
    YearMonth current = YearMonth.from(today);
    Map<YearMonth, Double> totals = new LinkedHashMap<>();

    for (int i = 11; i >= 0; i--) {
      YearMonth ym = current.minusMonths(i);
      totals.put(ym, 0.0);
    }

    for (Pedido pedido : pedidosCerrados) {
      LocalDateTime eventDate = closingDate(pedido);
      YearMonth ym = YearMonth.from(eventDate);
      if (totals.containsKey(ym)) {
        totals.put(ym, totals.get(ym) + effectiveAmount(pedido));
      }
    }

    return totals.entrySet().stream()
        .map(entry -> new MonthlyRevenuePoint(
            entry.getKey().atDay(1).format(MONTH_LABEL).replace(".", ""),
            round(entry.getValue())))
        .toList();
  }

  private List<StatusPoint> buildOrderStatus(List<Pedido> pedidos) {
    List<String> order = List.of("REGISTRADO", "EN_PREPARACION", "LISTO", "EN_RUTA", "ENTREGADO", "PAGADO", "ANULADO");
    Map<String, Long> counts = pedidos.stream()
        .collect(Collectors.groupingBy(p -> normalize(p.getEstado()), Collectors.counting()));

    List<StatusPoint> points = new ArrayList<>();
    for (String status : order) {
      points.add(new StatusPoint(status, counts.getOrDefault(status, 0L)));
    }
    return points;
  }

  private List<TopProduct> buildTopProducts(List<Pedido> pedidos) {
    Map<String, AggregateProduct> aggregate = new LinkedHashMap<>();

    for (Pedido pedido : pedidos) {
      if (pedido.getDetalles() == null) {
        continue;
      }

      for (DetallePedido detalle : pedido.getDetalles()) {
        if (detalle.getProducto() == null) {
          continue;
        }

        String key = detalle.getProducto().getNombre();
        AggregateProduct current = aggregate.computeIfAbsent(key,
            unused -> new AggregateProduct(detalle.getProducto().getNombre()));
        current.quantity += detalle.getCantidad() == null ? 0 : detalle.getCantidad();
        current.total += detalle.getSubtotal() == null ? 0.0 : detalle.getSubtotal();
      }
    }

    List<AggregateProduct> ranked = aggregate.values().stream()
        .sorted(Comparator.comparingLong(AggregateProduct::quantity).reversed()
            .thenComparing(Comparator.comparingDouble(AggregateProduct::total).reversed()))
        .toList();

    long maxQuantity = ranked.isEmpty() ? 0L : ranked.get(0).quantity();

    return ranked.stream()
        .limit(5)
        .map(item -> new TopProduct(
            item.name(),
            item.quantity(),
            formatCurrency(item.total()),
            maxQuantity == 0L ? 0.0 : round((item.quantity() * 100.0) / maxQuantity)))
        .toList();
  }

  private List<RecentOrder> buildRecentOrders(List<Pedido> pedidos) {
    return pedidos.stream()
        .limit(8)
        .map(this::toRecentOrder)
        .toList();
  }

  private List<RecentIncident> buildRecentIncidents(List<Incidencia> incidencias) {
    return incidencias.stream()
        .limit(6)
        .map(this::toRecentIncident)
        .toList();
  }

  private RecentOrder toRecentOrder(Pedido pedido) {
    return new RecentOrder(
        safeText(pedido.getNombreCliente(), "Cliente general"),
        displayModality(pedido.getModalidad()),
        displayStatus(pedido.getEstado()),
        statusClass(pedido.getEstado()),
        formatDateTime(eventDate(pedido)),
        pedido.getMesa() != null ? "Mesa #" + pedido.getMesa().getNumero() : displayModality(pedido.getModalidad()),
        formatCurrency(effectiveAmount(pedido)));
  }

  private RecentIncident toRecentIncident(Incidencia incidencia) {
    return new RecentIncident(
        safeText(incidencia.getTitulo(), "Sin titulo"),
        safeText(incidencia.getTipo(), "General"),
        displaySeverity(incidencia.getNivelGravedad()),
        severityClass(incidencia.getNivelGravedad()),
        displayIncidentStatus(incidencia.getEstado()),
        incidentStatusClass(incidencia.getEstado()),
        incidencia.getFechaReporte() != null ? incidencia.getFechaReporte().format(DATE_TIME) : "-",
        incidencia.getUsuarioReporta() != null
            ? incidencia.getUsuarioReporta().getNombre() + " " + incidencia.getUsuarioReporta().getApellidos()
            : "Sistema");
  }

  private LowStockItem toLowStockItem(Insumo insumo) {
    return new LowStockItem(
        safeText(insumo.getNombre(), "Insumo"),
        insumo.getUnidadMedida() != null ? safeText(insumo.getUnidadMedida().getNombre(), "UND") : "UND",
        round(insumo.getStockActual()),
        round(insumo.getStockMinimo()),
        stockLabel(insumo),
        stockClass(insumo));
  }

  private boolean isClosed(Pedido pedido) {
    return CLOSED_STATES.contains(normalize(pedido.getEstado()));
  }

  private double sumRevenue(List<Pedido> pedidos, LocalDateTime from, LocalDateTime to) {
    return pedidos.stream()
        .filter(p -> {
          LocalDateTime eventDate = eventDate(p);
          return !eventDate.isBefore(from) && eventDate.isBefore(to);
        })
        .mapToDouble(this::effectiveAmount)
        .sum();
  }

  private long countClosedBetween(List<Pedido> pedidos, LocalDateTime from, LocalDateTime to) {
    return pedidos.stream()
        .filter(p -> {
          LocalDateTime eventDate = eventDate(p);
          return !eventDate.isBefore(from) && eventDate.isBefore(to);
        })
        .count();
  }

  private LocalDateTime eventDate(Pedido pedido) {
    if (pedido.getFechaCompletado() != null) {
      return pedido.getFechaCompletado();
    }
    return pedido.getFechaRegistro() != null ? pedido.getFechaRegistro() : LocalDateTime.now();
  }

  private LocalDateTime closingDate(Pedido pedido) {
    return pedido.getFechaCompletado() != null ? pedido.getFechaCompletado() : eventDate(pedido);
  }

  private double effectiveAmount(Pedido pedido) {
    return round((pedido.getTotal() == null ? 0.0 : pedido.getTotal())
        + (pedido.getCostoEnvio() == null ? 0.0 : pedido.getCostoEnvio()));
  }

  private double stockGap(Insumo insumo) {
    return (insumo.getStockActual() == null ? 0.0 : insumo.getStockActual())
        - (insumo.getStockMinimo() == null ? 0.0 : insumo.getStockMinimo());
  }

  private String formatGrowth(double previous, double current) {
    if (previous <= 0.0) {
      return current > 0.0 ? "Nuevo" : "0%";
    }
    double growth = ((current - previous) / previous) * 100.0;
    return (growth >= 0 ? "+" : "") + formatPercent(growth);
  }

  private String formatPercent(double value) {
    NumberFormat percent = NumberFormat.getNumberInstance(LOCALE);
    percent.setMaximumFractionDigits(1);
    percent.setMinimumFractionDigits(1);
    return percent.format(round(value));
  }

  private String formatCurrency(double amount) {
    NumberFormat currency = NumberFormat.getCurrencyInstance(LOCALE);
    currency.setMinimumFractionDigits(2);
    currency.setMaximumFractionDigits(2);
    return currency.format(round(amount));
  }

  private double round(double amount) {
    return Math.round(amount * 100.0) / 100.0;
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toUpperCase();
  }

  private String safeText(String value, String fallback) {
    if (value == null || value.trim().isEmpty()) {
      return fallback;
    }
    return value.trim();
  }

  private String formatDateTime(LocalDateTime value) {
    return value == null ? "-" : value.format(DATE_TIME);
  }

  private String displayModality(String modality) {
    return switch (normalize(modality)) {
      case "LOCAL" -> "Local";
      case "LLEVAR" -> "Llevar";
      case "DELIVERY" -> "Delivery";
      default -> safeText(modality, "Sin modalidad");
    };
  }

  private String displayStatus(String status) {
    return switch (normalize(status)) {
      case "REGISTRADO" -> "Registrado";
      case "EN_PREPARACION" -> "En preparacion";
      case "LISTO" -> "Listo";
      case "EN_RUTA" -> "En ruta";
      case "ENTREGADO" -> "Entregado";
      case "PAGADO" -> "Pagado";
      case "ANULADO" -> "Anulado";
      default -> safeText(status, "Sin estado");
    };
  }

  private String statusClass(String status) {
    return switch (normalize(status)) {
      case "REGISTRADO" -> "bg-secondary-subtle text-secondary";
      case "EN_PREPARACION" -> "bg-warning-subtle text-warning";
      case "LISTO" -> "bg-primary-subtle text-primary";
      case "EN_RUTA" -> "bg-info-subtle text-info";
      case "ENTREGADO", "PAGADO" -> "bg-success-subtle text-success";
      case "ANULADO" -> "bg-danger-subtle text-danger";
      default -> "bg-light text-body";
    };
  }

  private String displaySeverity(String severity) {
    return switch (normalize(severity)) {
      case "BAJO" -> "Bajo";
      case "MEDIO" -> "Medio";
      case "EMERGENCIA" -> "Emergencia";
      default -> safeText(severity, "Sin nivel");
    };
  }

  private String severityClass(String severity) {
    return switch (normalize(severity)) {
      case "BAJO" -> "bg-info-subtle text-info";
      case "MEDIO" -> "bg-warning-subtle text-warning";
      case "EMERGENCIA" -> "bg-danger-subtle text-danger";
      default -> "bg-light text-body";
    };
  }

  private String displayIncidentStatus(String status) {
    return switch (normalize(status)) {
      case "PENDIENTE" -> "Pendiente";
      case "EN_PROCESO" -> "En proceso";
      case "RESUELTA" -> "Resuelta";
      case "CERRADA" -> "Cerrada";
      default -> safeText(status, "Sin estado");
    };
  }

  private String incidentStatusClass(String status) {
    return switch (normalize(status)) {
      case "PENDIENTE" -> "bg-warning-subtle text-warning";
      case "EN_PROCESO" -> "bg-info-subtle text-info";
      case "RESUELTA" -> "bg-success-subtle text-success";
      case "CERRADA" -> "bg-secondary-subtle text-secondary";
      default -> "bg-light text-body";
    };
  }

  private String stockClass(Insumo insumo) {
    double actual = insumo.getStockActual() == null ? 0.0 : insumo.getStockActual();
    double minimo = insumo.getStockMinimo() == null ? 0.0 : insumo.getStockMinimo();
    if (actual <= 0) {
      return "bg-danger-subtle text-danger";
    }
    if (actual <= minimo) {
      return "bg-warning-subtle text-warning";
    }
    return "bg-secondary-subtle text-secondary";
  }

  private String stockLabel(Insumo insumo) {
    double actual = insumo.getStockActual() == null ? 0.0 : insumo.getStockActual();
    double minimo = insumo.getStockMinimo() == null ? 0.0 : insumo.getStockMinimo();
    if (actual <= 0) {
      return "Sin stock";
    }
    if (actual <= minimo) {
      return "Bajo stock";
    }
    return "Alerta";
  }

  private static final class AggregateProduct {
    private final String name;
    private long quantity;
    private double total;

    private AggregateProduct(String name) {
      this.name = name;
    }

    private String name() {
      return name;
    }

    private long quantity() {
      return quantity;
    }

    private double total() {
      return total;
    }
  }
}
