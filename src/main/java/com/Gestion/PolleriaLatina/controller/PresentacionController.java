package com.Gestion.PolleriaLatina.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Presentacion;
import com.Gestion.PolleriaLatina.service.PresentacionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/presentaciones")
@PreAuthorize("hasAuthority('PRESENTACIONES_VER')")
@RequiredArgsConstructor
public class PresentacionController {

    private final PresentacionService presentacionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("presentaciones", presentacionService.listarTodas());
        model.addAttribute("presentacion", new Presentacion());
        model.addAttribute("titulo", "Gestión de Presentaciones");
        return "modules/catalogo/presentaciones";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("presentacion") Presentacion pres, RedirectAttributes flash) {
        try {
            presentacionService.guardar(pres);
            flash.addFlashAttribute("success", "Presentación guardada con éxito.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/presentaciones";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            presentacionService.eliminar(id);
            flash.addFlashAttribute("success", "Presentación dada de baja.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/presentaciones";
    }

}
