package com.Gestion.PolleriaLatina.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Categoria;
import com.Gestion.PolleriaLatina.service.CategoriaService;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/categorias")
@PreAuthorize("hasAuthority('CATEGORIAS_VER')")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("titulo", "Gestión de Categorías");
        return "modules/catalogo/categorias"; 
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute("categoria") Categoria category, RedirectAttributes flash, Model model) {
        try {
            categoriaService.guardar(category);
            flash.addFlashAttribute("success", "La categoría se ha guardado correctamente.");
            return "redirect:/categorias";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Gestión de Categorías - Error");
            return "modules/catalogo/categorias";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaService.eliminar(id);
            flash.addFlashAttribute("success", "Categoría eliminada correctamente del catálogo.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
