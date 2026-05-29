package com.Gestion.PolleriaLatina.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.Gestion.PolleriaLatina.service.SalonService;

@Controller
@RequestMapping("/cocina")
@PreAuthorize("hasAuthority('COCINA_VER')")
@RequiredArgsConstructor
public class CocinaController {

    private final SalonService salonService;

    @GetMapping
    @Transactional(readOnly = true)
    public String verPanelCocina(Model model) {
        model.addAttribute("tickets", salonService.listarPedidosCocina());
        model.addAttribute("titulo", "Panel de Control de Cocina");
        return "modules/operaciones/cocina";
    }

    @PostMapping("/actualizar-estado")
    public String actualizarEstadoTicket(@RequestParam("pedidoId") Long pedidoId,
                                         @RequestParam("nuevoEstado") String nuevoEstado,
                                         RedirectAttributes flash) {
        try {
            salonService.cambiarEstadoCocina(pedidoId, nuevoEstado);
            flash.addFlashAttribute("success", "Ticket actualizado en tiempo real.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cocina";
    }

}
