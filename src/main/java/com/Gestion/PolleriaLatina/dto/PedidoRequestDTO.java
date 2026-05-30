package com.Gestion.PolleriaLatina.dto;

import java.util.List;

import lombok.Data;

@Data
public class PedidoRequestDTO {

  private Long pedidoId;

  private String modalidad;
  private String nombreCliente;
  private String notasAdicionales;

  private Integer numeroMesa;

  private String telefonoContacto;
  private String direccionEntrega;
  private String referenciaEntrega;

  private boolean requiereCobroInmediato;
  private String metodoPago;
  private String tipoComprobante;
  private String documentoCliente;
  private Double totalCalculado;

  private List<ItemCarritoDTO> items;

  @Data
  public static class ItemCarritoDTO {
    private Long productoId;
    private Integer cantidad;
    private Double subtotal;
  }
}