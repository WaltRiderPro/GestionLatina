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

import com.Gestion.PolleriaLatina.model.UnidadMedida;
import com.Gestion.PolleriaLatina.service.UnidadMedidaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/unidades-medida")
@PreAuthorize("hasAuthority('UNIDADES_VER')")
@RequiredArgsConstructor
public class UnidadMedidaController {

  private final UnidadMedidaService unidadMedidaService;

  @GetMapping
  public String listarUnidades(Model model) {
    model.addAttribute("unidades", unidadMedidaService.listarTodas());
    model.addAttribute("unidadForm", new UnidadMedida());
    model.addAttribute("titulo", "Mantenimiento de Unidades de Medida");
    return "modules/almacen/unidades-medida";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAnyAuthority('UNIDADES_CREAR', 'UNIDADES_EDITAR')")
  public String guardarUnidad(@ModelAttribute("unidadForm") UnidadMedida unidad, RedirectAttributes flash) {
    try {
      unidadMedidaService.guardar(unidad);
      flash.addFlashAttribute("success", "Unidad de medida guardada correctamente.");
    } catch (RuntimeException e) {

      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/unidades-medida";
  }

  @GetMapping("/estado/{id}")
  @PreAuthorize("hasAuthority('UNIDADES_EDITAR')")
  public String cambiarEstado(@PathVariable Long id, RedirectAttributes flash) {
    try {
      unidadMedidaService.cambiarEstadoActivo(id);
      flash.addFlashAttribute("success", "El estado de la unidad fue modificado.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/unidades-medida";
  }

  @GetMapping("/eliminar/{id}")
  @PreAuthorize("hasAuthority('UNIDADES_ELIMINAR')")
  public String eliminarUnidad(@PathVariable Long id, RedirectAttributes flash) {
    try {
      unidadMedidaService.eliminarLogico(id);
      flash.addFlashAttribute("success", "Unidad de medida eliminada correctamente.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/unidades-medida";
  }
}