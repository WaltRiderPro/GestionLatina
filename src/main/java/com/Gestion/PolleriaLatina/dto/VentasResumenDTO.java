package com.Gestion.PolleriaLatina.dto;

import java.util.List;
import java.util.Map;

import com.Gestion.PolleriaLatina.model.Venta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentasResumenDTO {
  private List<Venta> ventas;
  private long totalVentas;
  private double montoTotal;
  private long ventasHoy;
  private double montoHoy;
  private long ventasMes;
  private double montoMes;
  private double ticketPromedio;
  private Map<String, Long> ventasPorMetodoPago;
  private String filtroTexto;
  private String filtroMetodoPago;
  private String filtroDesde;
  private String filtroHasta;
}
