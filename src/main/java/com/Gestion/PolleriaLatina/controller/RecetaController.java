package com.Gestion.PolleriaLatina.controller;

import java.util.List;

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
import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.model.Receta;
import com.Gestion.PolleriaLatina.repository.ProductoRepository;
import com.Gestion.PolleriaLatina.service.InsumoService;
import com.Gestion.PolleriaLatina.service.RecetaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/recetas")
@PreAuthorize("hasAuthority('RECETAS_VER')")
@RequiredArgsConstructor
public class RecetaController {

  private final RecetaService recetaService;
  private final InsumoService insumoService;
  private final ProductoRepository productoRepository;

  // 1. LANDING: Listar los productos de la carta optimizados con FETCH
  @GetMapping
  @Transactional(readOnly = true)
  public String listarProductosCarta(Model model) {

    List<Producto> productosCarta = productoRepository.findByEliminadoFalseOrderByNombreAsc();
    model.addAttribute("productos", productosCarta);
    model.addAttribute("titulo", "Fórmulas y Recetas por Plato");
    return "modules/almacen/recetas";
  }

  @GetMapping("/producto/{productoId}")
  @Transactional(readOnly = true)
  public String verRecetaProducto(@PathVariable Long productoId, Model model, RedirectAttributes flash) {

    Producto producto = productoRepository.findByIdConCategoriasYPresentaciones(productoId).orElse(null);

    if (producto == null) {
      flash.addFlashAttribute("error", "El producto de la carta solicitado no existe.");
      return "redirect:/recetas";
    }

    Receta nuevaReceta = Receta.builder()
        .producto(producto)
        .build();

    List<Receta> ingredientesActuales = recetaService.listarIngredientesPorProducto(productoId);
    List<Insumo> insumosDisponibles = insumoService.listarInsumosActivos();

    model.addAttribute("producto", producto);
    model.addAttribute("ingredientes", ingredientesActuales);
    model.addAttribute("insumos", insumosDisponibles);
    model.addAttribute("nuevaReceta", nuevaReceta);
    model.addAttribute("titulo", "Fórmula de Cocina: " + producto.getNombre());
    return "modules/almacen/receta-detalle";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAnyAuthority('RECETAS_CREAR', 'RECETAS_EDITAR')")
  public String guardarIngrediente(@ModelAttribute("nuevaReceta") Receta receta, RedirectAttributes flash) {
    Long productoId = receta.getProducto().getId();
    try {
      recetaService.guardarIngrediente(receta);
      flash.addFlashAttribute("success", "Ingrediente integrado a la receta de producción correctamente.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/recetas/producto/" + productoId;
  }

  @GetMapping("/eliminar/{id}/producto/{productoId}")
  @PreAuthorize("hasAuthority('RECETAS_ELIMINAR')")
  public String eliminarIngrediente(@PathVariable Long id, @PathVariable Long productoId, RedirectAttributes flash) {
    try {
      recetaService.eliminarIngrediente(id);
      flash.addFlashAttribute("success", "El ingrediente fue removido de la receta con éxito.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/recetas/producto/" + productoId;
  }
}