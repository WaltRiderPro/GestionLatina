package com.Gestion.PolleriaLatina.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Insumo;
import com.Gestion.PolleriaLatina.model.KardexMovimiento;
import com.Gestion.PolleriaLatina.service.InsumoService;
import com.Gestion.PolleriaLatina.service.KardexService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/kardex")
@PreAuthorize("hasAuthority('KARDEX_VER')")
@RequiredArgsConstructor
public class KardexController {

  private final KardexService kardexService;
  private final InsumoService insumoService;

  @GetMapping
  @Transactional(readOnly = true)
  public String verKardexGeneral(Model model) {
    model.addAttribute("movimientosGlobales", kardexService.obtenerHistorialCompleto());
    model.addAttribute("titulo", "Registro Histórico de Operaciones");
    return "modules/almacen/kardex";
  }

  @GetMapping("/insumo/{id}")
  @Transactional(readOnly = true)
  public String verKardexPorInsumo(@PathVariable Long id, Model model, RedirectAttributes flash) {
    Insumo insumo = insumoService.obtenerPorId(id);
    if (insumo == null) {
      flash.addFlashAttribute("error", "El insumo solicitado para auditoría no existe.");
      return "redirect:/insumos";
    }

    List<KardexMovimiento> movimientosInsumo = kardexService.obtenerHistorialPorInsumo(id);

    model.addAttribute("insumo", insumo);
    model.addAttribute("movimientos", movimientosInsumo);
    model.addAttribute("titulo", "Detalle de Kardex: " + insumo.getNombre());
    return "modules/almacen/kardex-insumo";
  }
}