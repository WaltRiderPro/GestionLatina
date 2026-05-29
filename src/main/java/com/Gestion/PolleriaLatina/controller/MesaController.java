package com.Gestion.PolleriaLatina.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Mesa;
import com.Gestion.PolleriaLatina.service.SalonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mesas")
@PreAuthorize("hasAuthority('MESAS_VER')") 
@RequiredArgsConstructor
public class MesaController {

    private final SalonService salonService;

    @GetMapping
    public String verSalon(Model model) {
        model.addAttribute("mesas", salonService.listarMesas());
        model.addAttribute("productos", salonService.listarProductosDisponibles());
        model.addAttribute("mesaObj", new Mesa()); 
        model.addAttribute("titulo", "Gestión de Salón y Mesas");
        return "modules/salon/mesas";
    }

    @PostMapping("/guardar")
    public String guardarMesa(@ModelAttribute("mesaObj") Mesa mesa, RedirectAttributes flash) {
        try {
            salonService.guardarMesa(mesa);
            flash.addFlashAttribute("success", "Estructura de mesa registrada con éxito.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMesa(@PathVariable Long id, RedirectAttributes flash) {
        try {
            salonService.eliminarMesa(id);
            flash.addFlashAttribute("success", "Mesa removida del salón correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @PostMapping("/abrir")
    public String abrirMesa(@RequestParam("mesaId") Long mesaId,
                            @RequestParam("nombreCliente") String nombreCliente,
                            @RequestParam("notasAdicionales") String notas,
                            RedirectAttributes flash) {
        try {
            salonService.abrirPedido(mesaId, nombreCliente, notas);
            flash.addFlashAttribute("success", "Mesa ocupada. Comanda enviada a cocina.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @PostMapping("/agregar-producto")
    public String agregarProducto(@RequestParam("pedidoId") Long pedidoId,
                                  @RequestParam("productoId") Long productoId,
                                  @RequestParam("cantidad") Integer cantidad,
                                  RedirectAttributes flash) {
        try {
            salonService.agregarProductoAPedido(pedidoId, productoId, cantidad);
            flash.addFlashAttribute("success", "Adición registrada de forma acumulativa.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

}
