package com.Gestion.PolleriaLatina.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.model.KardexMovimiento;
import com.Gestion.PolleriaLatina.model.Mesa;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.model.Receta;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.model.enumerados.TipoMovimiento;
import com.Gestion.PolleriaLatina.repository.KardexMovimientoRepository;
import com.Gestion.PolleriaLatina.repository.MesaRepository;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.repository.ProductoRepository;
import com.Gestion.PolleriaLatina.repository.RecetaRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalonServiceImpl implements SalonService {

  private final MesaRepository mesaRepository;
  private final PedidoRepository pedidoRepository;
  private final ProductoRepository productoRepository;
  private final RecetaRepository recetaRepository;
  private final UsuarioRepository usuarioRepository;
  private final KardexMovimientoRepository kardexMovimientoRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Mesa> listarMesas() {
    return mesaRepository.findByActivoTrueAndEliminadoFalseOrderByNumeroAsc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Producto> listarProductosDisponibles() {
    List<Producto> productos = productoRepository.findActiveProductsWithRelations();

    for (Producto p : productos) {
      List<Receta> recetaPlato = recetaRepository.findByProductoId(p.getId());
      boolean hayStock = true;

      for (Receta receta : recetaPlato) {
        if (receta.getInsumo().getStockActual() < receta.getCantidadNecesaria()) {
          hayStock = false;
          break;
        }
      }
      p.setSuficienteStock(hayStock);
    }

    return productos;
  }

  @Override
  @Transactional(readOnly = true)
  public Pedido obtenerPedidoActivoPorMesa(Long mesaId) {
    List<Pedido> pedidos = pedidoRepository.findActiveOrderByMesaId(mesaId);
    return pedidos.isEmpty() ? null : pedidos.get(0);
  }

  @Override
  @Transactional
  public void abrirPedido(Long mesaId, String nombreCliente, String notas) {
    Mesa mesa = mesaRepository.findById(mesaId)
        .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

    if (!"LIBRE".equals(mesa.getEstado())) {
      throw new RuntimeException("La mesa seleccionada ya no está disponible.");
    }

    Pedido pedido = Pedido.builder()
        .nombreCliente((nombreCliente == null || nombreCliente.trim().isEmpty()) ? "Mesa " + mesa.getNumero()
            : nombreCliente.trim())
        .modalidad("LOCAL")
        .estado("REGISTRADO")
        .fechaRegistro(LocalDateTime.now())
        .notasAdicionales(notas)
        .mesa(mesa)
        .total(0.0)
        .detalles(new ArrayList<>())
        .build();

    mesa.setEstado("OCUPADA");
    mesaRepository.save(mesa);
    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void agregarProductoAPedido(Long pedidoId, Long productoId, Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
      throw new RuntimeException("La cantidad ingresada debe ser mayor a cero.");
    }

    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("Comanda no encontrada."));

    Producto producto = productoRepository.findById(productoId)
        .orElseThrow(() -> new RuntimeException("Producto no existente en catálogo."));

    Optional<DetallePedido> detalleExistente = pedido.getDetalles().stream()
        .filter(d -> d.getProducto().getId().equals(productoId))
        .findFirst();

    if (detalleExistente.isPresent()) {
      DetallePedido detalle = detalleExistente.get();
      detalle.setCantidad(detalle.getCantidad() + cantidad);
      detalle.setSubtotal(detalle.getCantidad() * producto.getPrecio());
    } else {
      DetallePedido nuevoDetalle = DetallePedido.builder()
          .pedido(pedido)
          .producto(producto)
          .cantidad(cantidad)
          .subtotal(cantidad * producto.getPrecio())
          .build();
      pedido.getDetalles().add(nuevoDetalle);
    }

    double nuevoTotal = pedido.getDetalles().stream()
        .mapToDouble(DetallePedido::getSubtotal)
        .sum();
    pedido.setTotal(nuevoTotal);

    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void guardarMesa(Mesa mesa) {
    if (mesa.getId() == null) {
      mesa.setEstado("LIBRE");
      mesa.setActivo(true);
      mesa.setEliminado(false);
    } else {
      Mesa mesaExistente = mesaRepository.findById(mesa.getId())
          .orElseThrow(() -> new RuntimeException("Mesa a editar no encontrada."));
      mesa.setEstado(mesaExistente.getEstado());
      mesa.setActivo(mesaExistente.isActivo());
      mesa.setEliminado(mesaExistente.isEliminado());
    }
    mesaRepository.save(mesa);
  }

  @Override
  @Transactional
  public void eliminarMesa(Long id) {
    Mesa mesa = mesaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Mesa no encontrada."));
    if ("OCUPADA".equals(mesa.getEstado())) {
      throw new RuntimeException("Operación denegada: No se puede eliminar una mesa con comanda activa.");
    }
    mesaRepository.delete(mesa);
  }

  @Override
  @Transactional
  public void registrarPedidoSalon(Long mesaId, String nombreCliente, String notas, List<Long> productoIds,
      List<Integer> cantidades) {
    if (productoIds == null || productoIds.isEmpty() || cantidades == null || cantidades.isEmpty()) {
      throw new RuntimeException("Error: No se puede enviar a cocina una comanda sin productos.");
    }

    Mesa mesa = mesaRepository.findById(mesaId)
        .orElseThrow(() -> new RuntimeException("Mesa no encontrada."));

    if (!"LIBRE".equals(mesa.getEstado())) {
      throw new RuntimeException("La mesa seleccionada ya no se encuentra disponible.");
    }

    Pedido pedido = Pedido.builder()
        .nombreCliente((nombreCliente == null || nombreCliente.trim().isEmpty()) ? "Mesa #" + mesa.getNumero()
            : nombreCliente.trim())
        .modalidad("LOCAL")
        .estado("REGISTRADO")
        .fechaRegistro(LocalDateTime.now())
        .notasAdicionales(notas)
        .mesa(mesa)
        .total(0.0)
        .detalles(new ArrayList<>())
        .build();

    double totalAcumulado = 0.0;

    for (int i = 0; i < productoIds.size(); i++) {
      Long prodId = productoIds.get(i);
      Integer cant = cantidades.get(i);

      if (cant == null || cant <= 0)
        continue;

      Producto producto = productoRepository.findById(prodId)
          .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + prodId));

      DetallePedido detalle = DetallePedido.builder()
          .pedido(pedido)
          .producto(producto)
          .cantidad(cant)
          .subtotal(producto.getPrecio() * cant)
          .build();

      pedido.getDetalles().add(detalle);
      totalAcumulado += detalle.getSubtotal();
    }

    pedido.setTotal(totalAcumulado);

    mesa.setEstado("OCUPADA");
    mesaRepository.save(mesa);

    pedidoRepository.save(pedido);
  }

  @Override
  @Transactional
  public void cancelarPedidoSalon(Long pedidoId) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

    if (pedido.getMesa() != null) {
      Mesa mesa = pedido.getMesa();
      mesa.setEstado("LIBRE");
      mesaRepository.save(mesa);
    }

    pedidoRepository.delete(pedido);
  }

  @Override
  @Transactional
  public void pagarPedidoSalon(Long pedidoId, String metodoPago) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

    this.descontarInsumosDelAlmacen(pedido);

    pedido.setEstado("ENTREGADO");
    pedido.setFechaCompletado(LocalDateTime.now());
    pedido.setNotasAdicionales(pedido.getNotasAdicionales() + " | Pago: " + metodoPago);

    if (pedido.getMesa() != null) {
      Mesa mesa = pedido.getMesa();
      mesa.setEstado("LIBRE");
      mesaRepository.save(mesa);
    }

    pedidoRepository.save(pedido);
  }

  private void descontarInsumosDelAlmacen(Pedido pedido) {
    // Obtenemos el usuario logueado en el sistema actualmente (el cocinero)
    String usernameActual = org.springframework.security.core.context.SecurityContextHolder.getContext()
        .getAuthentication().getName();
    Usuario usuarioCocina = usuarioRepository.findByUsername(usernameActual).orElse(null);

    for (DetallePedido detalle : pedido.getDetalles()) {
      Long productoId = detalle.getProducto().getId();
      Integer cantidadVendida = detalle.getCantidad();

      List<Receta> formulasInsumos = recetaRepository.findByProductoId(productoId);

      for (Receta receta : formulasInsumos) {
        Insumo insumo = receta.getInsumo();

        double cantidadGastada = receta.getCantidadNecesaria() * cantidadVendida;
        double nuevoStock = insumo.getStockActual() - cantidadGastada;

        insumo.setStockActual(nuevoStock);

        if (nuevoStock < insumo.getStockMinimo()) {
          System.out.println("🚨 ALERTA: Insumo [" + insumo.getNombre() + "] bajo mínimo.");
        }

        if (usuarioCocina != null) {
          KardexMovimiento mov = KardexMovimiento.builder()
              .insumo(insumo)
              .tipoMovimiento(TipoMovimiento.SALIDA)
              .cantidad(cantidadGastada)
              .stockResultante(nuevoStock)
              .origen("Comanda #" + pedido.getId())
              .observacion("Preparación de " + cantidadVendida + "x " + detalle.getProducto().getNombre())
              .usuario(usuarioCocina)
              .build();

          kardexMovimientoRepository.save(mov);
        }
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Pedido> listarPedidosCocina() {
    return pedidoRepository.findPedidosParaCocina();
  }

  @Override
  @Transactional
  public void cambiarEstadoCocina(Long pedidoId, String nuevoEstado) {
    Pedido pedido = pedidoRepository.findById(pedidoId)
        .orElseThrow(() -> new RuntimeException("El ticket de cocina consultado no existe."));

    if ("EN_PREPARACION".equals(nuevoEstado)) {
      if ("REGISTRADO".equals(pedido.getEstado())) {
        this.descontarInsumosDelAlmacen(pedido);
      }
      pedido.setEstado(nuevoEstado);
      pedidoRepository.save(pedido);

    } else if ("LISTO".equals(nuevoEstado)) {
      pedido.setEstado(nuevoEstado);
      pedidoRepository.save(pedido);

    } else {
      throw new RuntimeException("Estado de cocina no válido.");
    }
  }

}
