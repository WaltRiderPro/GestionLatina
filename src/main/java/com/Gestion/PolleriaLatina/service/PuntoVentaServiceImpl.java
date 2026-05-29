package com.Gestion.PolleriaLatina.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.dto.PedidoRequestDTO;
import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.model.Venta;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.repository.ProductoRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;
import com.Gestion.PolleriaLatina.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PuntoVentaServiceImpl implements PuntoVentaService {

  private final PedidoRepository pedidoRepository;
  private final ProductoRepository productoRepository;
  private final VentaRepository ventaRepository;
  private final UsuarioRepository usuarioRepository;

  @Override
  @Transactional
  public Pedido procesarNuevaComanda(PedidoRequestDTO request, String usernameCajero) {

    Usuario cajero = usuarioRepository.findByUsername(usernameCajero)
        .orElseThrow(() -> new RuntimeException("Cajero no autorizado o sesión inválida."));

    Pedido pedido = Pedido.builder()
        .nombreCliente(request.getNombreCliente() != null && !request.getNombreCliente().trim().isEmpty()
            ? request.getNombreCliente()
            : "Cliente General")
        .modalidad(request.getModalidad().toUpperCase())
        .estado("REGISTRADO")
        .fechaRegistro(LocalDateTime.now())
        .notasAdicionales(request.getNotasAdicionales())
        .build();
    if ("DELIVERY".equalsIgnoreCase(pedido.getModalidad())) {
      pedido.setDireccionEntrega(request.getDireccionEntrega());
      pedido.setReferenciaEntrega(request.getReferenciaEntrega());
      pedido.setTelefonoContacto(request.getTelefonoContacto());
      pedido.setCostoEnvio(0.0);
    } else if ("LOCAL".equalsIgnoreCase(pedido.getModalidad())) {

      // PRIMERO GESTION DE MESAS

      // Mesa mesa = mesaRepository.findById(request.getMesaId()).orElseThrow(...);
      // pedido.setMesa(mesa);
    }

    List<DetallePedido> detalles = new ArrayList<>();
    double totalBruto = 0.0;

    for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {
      Producto producto = productoRepository.findById(item.getProductoId())
          .orElseThrow(() -> new RuntimeException("Producto no encontrado en catálogo: " + item.getProductoId()));

      Double subtotalItem = producto.getPrecio() * item.getCantidad();
      totalBruto += subtotalItem;

      DetallePedido detalle = DetallePedido.builder()
          .pedido(pedido)
          .producto(producto)
          .cantidad(item.getCantidad())
          .subtotal(subtotalItem)
          .build();

      detalles.add(detalle);

      // FUTURO: Si activas el descuento automático de recetas en almacén,
      // la llamada a tu RecetaService iría exactamente en esta línea.
    }

    pedido.setDetalles(detalles);
    pedido.setTotal(totalBruto);

    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    if (request.isRequiereCobroInmediato()) {
      Venta venta = Venta.builder()
          .pedido(pedidoGuardado)
          .tipoComprobante(request.getTipoComprobante())
          .numeroComprobante("TICKET-PENDIENTE")
          .documentoCliente(request.getDocumentoCliente())
          .metodoPago(request.getMetodoPago())
          .montoTotal(totalBruto)
          .fechaEmision(LocalDateTime.now())
          .cajero(cajero)
          .build();

      ventaRepository.save(venta);
    }

    return pedidoGuardado;
  }
}