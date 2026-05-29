package com.Gestion.PolleriaLatina.dto;

import java.util.List;

import lombok.Data;

@Data
public class PedidoRequestDTO {
  // LUIS ESTO ES PARA POS DONT WORRY BRO

  private String modalidad; // "LOCAL", "LLEVAR", "DELIVERY"
  private String nombreCliente;
  private String notasAdicionales;

  // 2. Si es LOCAL (Consumo en Salón)
  private Long mesaId;

  // 3. Si es DELIVERY (Servicio a Domicilio)
  private String telefonoContacto;
  private String direccionEntrega;
  private String referenciaEntrega;

  // 4. Datos Financieros (Venta directa para LLEVAR / DELIVERY)
  // Para LOCAL estos campos vendrán vacíos/nulos desde el frontend
  private boolean requiereCobroInmediato; // Flag para saber si creamos la Venta ya mismo
  private String metodoPago; // "EFECTIVO", "YAPE", "PLIN", "TARJETA"
  private String tipoComprobante; // "TICKET", "BOLETA", "FACTURA"
  private String documentoCliente; // DNI o RUC
  private Double totalCalculado; // Para validar que el frontend y backend cuadren

  // 5. El Carrito de Compras (Lista de productos a descargar)
  private List<ItemCarritoDTO> items;

  // Sub-clase interna estática para mapear cada línea del carrito
  @Data
  public static class ItemCarritoDTO {
    private Long productoId;
    private Integer cantidad;
    private Double subtotal;
  }
}