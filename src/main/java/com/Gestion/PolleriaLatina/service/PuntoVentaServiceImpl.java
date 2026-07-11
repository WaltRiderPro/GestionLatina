package com.Gestion.PolleriaLatina.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.dto.PedidoRequestDTO;
import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Mesa;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.model.Venta;
import com.Gestion.PolleriaLatina.repository.MesaRepository;
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
  private final MesaRepository mesaRepository;

  @Override
  @Transactional
  public Pedido procesarNuevaComanda(PedidoRequestDTO request, String usernameCajero) {
    Usuario cajero = usuarioRepository.findByUsername(usernameCajero)
        .orElseThrow(() -> new RuntimeException("Cajero no autorizado o sesión inválida."));

    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new RuntimeException("El carrito está vacío. Agregue al menos un producto.");
    }

    String tipoComprobante = request.getTipoComprobante() != null && !request.getTipoComprobante().isBlank()
        ? request.getTipoComprobante().toUpperCase()
        : "TICKET";

    if (request.getModalidad() == null || request.getModalidad().isBlank()) {
      throw new RuntimeException("Debe indicar la modalidad de venta.");
    }

    Pedido pedido;
    double totalBruto = 0.0;

    if (request.getPedidoId() != null) {
      pedido = pedidoRepository.findById(request.getPedidoId())
          .orElseThrow(() -> new RuntimeException("El pedido original no existe."));

      totalBruto = pedido.getTotal();

      if (request.isRequiereCobroInmediato()) {
        pedido.setEstado("PAGADO");
        pedido.setFechaCompletado(LocalDateTime.now());

        if (pedido.getMesa() != null) {
          Mesa mesa = pedido.getMesa();
          mesa.setEstado("LIBRE");
          mesaRepository.save(mesa);
        }
      }
    } else {
      pedido = Pedido.builder()
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
        if (request.getNumeroMesa() == null) {
          throw new RuntimeException("Debe indicar el número de mesa.");
        }

        Mesa mesa = mesaRepository.findByNumeroAndEliminadoFalse(request.getNumeroMesa())
            .orElseThrow(() -> new RuntimeException("La mesa #" + request.getNumeroMesa() + " no existe."));

        if ("OCUPADA".equals(mesa.getEstado())) {
          throw new RuntimeException("La mesa #" + request.getNumeroMesa() + " ya está ocupada.");
        }

        mesa.setEstado("OCUPADA");
        mesaRepository.save(mesa);
        pedido.setMesa(mesa);
      }

      List<DetallePedido> detalles = new ArrayList<>();
      for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {
        Producto producto = productoRepository.findById(item.getProductoId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

        Double subtotalItem = producto.getPrecio() * item.getCantidad();
        totalBruto += subtotalItem;

        detalles.add(DetallePedido.builder()
            .pedido(pedido)
            .producto(producto)
            .cantidad(item.getCantidad())
            .subtotal(subtotalItem)
            .build());
      }

      pedido.setDetalles(detalles);
      pedido.setTotal(totalBruto);
    }

    Pedido pedidoGuardado = pedidoRepository.save(pedido);

    if (request.isRequiereCobroInmediato()) {
      String serieComprobante = switch (tipoComprobante) {
        case "BOLETA" -> "B001";
        case "FACTURA" -> "F001";
        default -> "TCK";
      };

      Venta venta = Venta.builder()
          .pedido(pedidoGuardado)
          .tipoComprobante(tipoComprobante)
          .numeroComprobante(serieComprobante + "-" + String.format("%07d", pedidoGuardado.getId()))
          .documentoCliente(request.getDocumentoCliente())
          .metodoPago(request.getMetodoPago() != null && !request.getMetodoPago().isBlank()
              ? request.getMetodoPago().toUpperCase()
              : "EFECTIVO")
          .montoTotal(totalBruto)
          .fechaEmision(LocalDateTime.now())
          .cajero(cajero)
          .build();

      ventaRepository.save(venta);
    }

    return pedidoGuardado;
  }
}
