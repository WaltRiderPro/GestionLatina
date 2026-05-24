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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.service.CategoriaService;
import com.Gestion.PolleriaLatina.service.PresentacionService;
import com.Gestion.PolleriaLatina.service.ProductoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/productos")
@PreAuthorize("hasAuthority('PRODUCTOS_VER')")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final PresentacionService presentacionService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("presentaciones", presentacionService.listarTodas());
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Catálogo de Productos");
        return "modules/catalogo/productos";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("producto") Producto producto,
            @RequestParam("files") List<MultipartFile> archivos, 
            RedirectAttributes flash) {
        try {
            productoService.guardar(producto, archivos);
            flash.addFlashAttribute("success", "Producto catalogado de forma exitosa.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            productoService.eliminar(id);
            flash.addFlashAttribute("success", "Producto retirado del catálogo.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

}
