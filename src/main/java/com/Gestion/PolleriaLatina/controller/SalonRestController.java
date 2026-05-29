package com.Gestion.PolleriaLatina.controller;

import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.service.SalonService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/salon/api")
@RequiredArgsConstructor
public class SalonRestController {

    private final SalonService salonService;

    @GetMapping("/pedido-activo/{mesaId}")
    public Map<String, Object> obtenerPedidoActivo(@PathVariable Long mesaId) {
        Map<String, Object> response = new HashMap<>();
        Pedido pedido = salonService.obtenerPedidoActivoPorMesa(mesaId);
        
        if (pedido != null) {
            response.put("id", pedido.getId());
            response.put("total", pedido.getTotal());
        }
        return response;
    }
    
}
