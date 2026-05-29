package com.Gestion.PolleriaLatina.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Gestion.PolleriaLatina.model.DetallePedido;
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.service.SalonService;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/mesas/api")
@RequiredArgsConstructor
public class MesaRestController {

    private final SalonService salonService;

    @GetMapping("/pedido-activo/{mesaId}")
    public Map<String, Object> obtenerPedidoActivo(@PathVariable Long mesaId) {
        Map<String, Object> response = new HashMap<>();
        Pedido pedido = salonService.obtenerPedidoActivoPorMesa(mesaId);
        
        if (pedido != null) {
            response.put("id", pedido.getId());
            response.put("nombreCliente", pedido.getNombreCliente());
            response.put("total", pedido.getTotal());
            response.put("notas", pedido.getNotasAdicionales());
            
            List<Map<String, Object>> lotePlatos = new ArrayList<>();
            for (DetallePedido dp : pedido.getDetalles()) {
                Map<String, Object> item = new HashMap<>();
                item.put("productoNombre", dp.getProducto().getNombre());
                item.put("precio", dp.getProducto().getPrecio());
                item.put("cantidad", dp.getCantidad());
                item.put("subtotal", dp.getSubtotal());
                lotePlatos.add(item);
            }
            response.put("detalles", lotePlatos);
        }
        return response;
    }
    
}
