package com.Gestion.PolleriaLatina.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.service.SalonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/salon")
@PreAuthorize("hasAuthority('PEDIDOS_VER')") 
@RequiredArgsConstructor
public class SalonController {

    private final SalonService salonService;

    @GetMapping
    @Transactional(readOnly = true)
    public String verSalon(Model model) {
        model.addAttribute("mesas", salonService.listarMesas());
        model.addAttribute("productos", salonService.listarProductosDisponibles());
        model.addAttribute("titulo", "Gestión de Salón - Mesas");
        return "modules/salon/mesas";
    }

    @PostMapping("/abrir")
    public String abrirMesa(@RequestParam("mesaId") Long mesaId,
                            @RequestParam("nombreCliente") String nombreCliente,
                            @RequestParam("notasAdicionales") String notas,
                            RedirectAttributes flash) {
        try {
            salonService.abrirPedido(mesaId, nombreCliente, notas);
            flash.addFlashAttribute("success", "Mesa abierta correctamente. Ya puedes añadir productos.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/salon";
    }

    @PostMapping("/agregar-producto")
    public String agregarProducto(@RequestParam("pedidoId") Long pedidoId,
                                  @RequestParam("productoId") Long productoId,
                                  @RequestParam("cantidad") Integer cantidad,
                                  RedirectAttributes flash) {
        try {
            salonService.agregarProductoAPedido(pedidoId, productoId, cantidad);
            flash.addFlashAttribute("success", "Producto añadido a la orden de la cocina.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/salon";
    }

}
