package com.Gestion.PolleriaLatina.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.service.InsumoService;
import com.Gestion.PolleriaLatina.service.UnidadMedidaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/insumos")
@PreAuthorize("hasAuthority('INSUMOS_VER')")
@RequiredArgsConstructor
public class InsumoController {

  private final InsumoService insumoService;
  private final UnidadMedidaService unidadMedidaService;

  @GetMapping
  @Transactional(readOnly = true)
  public String listarInsumos(Model model) {
    model.addAttribute("insumos", insumoService.listarInsumosActivos());
    model.addAttribute("titulo", "Inventario de Insumos (Stock)");
    return "modules/almacen/insumos";
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasAuthority('INSUMOS_CREAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioNuevo(Model model) {
    model.addAttribute("insumo", new Insumo());
    model.addAttribute("unidades", unidadMedidaService.listarActivos());
    model.addAttribute("titulo", "Registrar Nuevo Insumo");
    return "modules/almacen/insumo-form";
  }

  @GetMapping("/editar/{id}")
  @PreAuthorize("hasAuthority('INSUMOS_EDITAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes flash) {
    Insumo insumo = insumoService.obtenerPorId(id);

    if (insumo == null) {
      flash.addFlashAttribute("error", "El insumo solicitado no existe.");
      return "redirect:/insumos";
    }

    model.addAttribute("insumo", insumo);
    model.addAttribute("unidades", unidadMedidaService.listarActivos());
    model.addAttribute("titulo", "Editar Insumo: " + insumo.getNombre());
    return "modules/almacen/insumo-form";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAnyAuthority('INSUMOS_CREAR', 'INSUMOS_EDITAR')")
  public String guardarInsumo(@ModelAttribute Insumo insumo, RedirectAttributes flash, Model model) {
    try {
      insumoService.guardarInsumo(insumo);
      flash.addFlashAttribute("success", "El insumo se registró correctamente en el almacén.");
      return "redirect:/insumos";
    } catch (RuntimeException e) {
      model.addAttribute("error", e.getMessage());
      model.addAttribute("unidades", unidadMedidaService.listarActivos());
      model.addAttribute("titulo",
          insumo.getId() == null ? "Registrar Nuevo Insumo" : "Editar Insumo: " + insumo.getNombre());
      return "modules/almacen/insumo-form";
    }
  }

  @GetMapping("/estado/{id}")
  @PreAuthorize("hasAuthority('INSUMOS_EDITAR')")
  public String cambiarEstadoInsumo(@PathVariable Long id, RedirectAttributes flash) {
    try {
      insumoService.cambiarEstadoActivo(id);
      flash.addFlashAttribute("success", "El estado del insumo fue modificado.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/insumos";
  }

  @GetMapping("/eliminar/{id}")
  @PreAuthorize("hasAuthority('INSUMOS_ELIMINAR')")
  public String eliminarInsumo(@PathVariable Long id, RedirectAttributes flash) {
    try {
      insumoService.eliminarLogico(id);
      flash.addFlashAttribute("success", "Insumo dado de baja del sistema correctamente.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/insumos";
  }
}