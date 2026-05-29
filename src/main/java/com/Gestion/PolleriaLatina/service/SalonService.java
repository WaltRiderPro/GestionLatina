package com.Gestion.PolleriaLatina.service;

import java.util.List;
import com.Gestion.PolleriaLatina.model.Mesa;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.model.Producto;

public interface SalonService {

    List<Mesa> listarMesas();
    List<Producto> listarProductosDisponibles();
    Pedido obtenerPedidoActivoPorMesa(Long mesaId);
    void abrirPedido(Long mesaId, String nombreCliente, String notas);
    void agregarProductoAPedido(Long pedidoId, Long productoId, Integer cantidad);
    void guardarMesa(Mesa mesa);
    void eliminarMesa(Long id);
    void registrarPedidoSalon(Long mesaId, String nombreCliente, String notas, List<Long> productoIds, List<Integer> cantidades);
    void cancelarPedidoSalon(Long pedidoId);
    void pagarPedidoSalon(Long pedidoId, String metodoPago);
    List<Pedido> listarPedidosCocina();
    void cambiarEstadoCocina(Long pedidoId, String nuevoEstado);

}
