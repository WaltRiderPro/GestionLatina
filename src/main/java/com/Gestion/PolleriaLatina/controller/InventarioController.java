package com.Gestion.PolleriaLatina.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.service.InsumoService;
import com.Gestion.PolleriaLatina.service.InventarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/inventarios")
@PreAuthorize("hasAuthority('INVENTARIO_VER')")
@RequiredArgsConstructor
public class InventarioController {

  private final InventarioService inventarioService;
  private final InsumoService insumoService;

  // 1. AHORA LA RUTA BASE CARGA DIRECTAMENTE EL FORMULARIO DINÁMICO
  @GetMapping
  @Transactional(readOnly = true)
  public String iniciarTomaInventario(Model model) {
    model.addAttribute("insumos", insumoService.listarInsumosActivos());
    model.addAttribute("titulo", "Ajuste Masivo de Almacén");
    return "modules/almacen/inventario-form";
  }

  // 2. PROCESAR GUARDADO DINÁMICO Y REDIRECCIONAR AL MISMO FORMULARIO LIMPIO
  @PostMapping("/guardar")
  @PreAuthorize("hasAuthority('INVENTARIO_CREAR')")
  public String guardarInventario(
      @RequestParam("tipoMovimiento") String tipoMovimiento,
      @RequestParam("motivo") String motivo,
      @RequestParam(value = "referencia", required = false) String referencia,
      @RequestParam("insumoId[]") List<Long> insumoIds,
      @RequestParam("cantidad[]") List<Double> cantidades,
      Authentication auth, RedirectAttributes flash) {
    try {
      inventarioService.procesarAjusteMasivo(tipoMovimiento, motivo, referencia, insumoIds, cantidades, auth.getName());
      flash.addFlashAttribute("success",
          "Lote de inventario procesado con éxito. Los movimientos se han asentado de forma inmutable en el Kardex.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Error operativo al procesar el ajuste: " + e.getMessage());
    }
    return "redirect:/inventarios"; 
  }
}