package com.Gestion.PolleriaLatina.controller;

import java.util.List;

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
import com.Gestion.PolleriaLatina.model.Pedido;
import com.Gestion.PolleriaLatina.repository.MesaRepository;
import com.Gestion.PolleriaLatina.repository.PedidoRepository;
import com.Gestion.PolleriaLatina.service.CategoriaService;
import com.Gestion.PolleriaLatina.service.SalonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mesas")
@PreAuthorize("hasAuthority('PEDIDOS_VER')")
@RequiredArgsConstructor
public class MesaController {

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;
    private final CategoriaService categoriaService;
    private final SalonService salonService;

    @GetMapping
    public String listarSalon(Model model) {
        model.addAttribute("mesas", mesaRepository.findByActivoTrueAndEliminadoFalseOrderByNumeroAsc());
        model.addAttribute("mesaObj", new Mesa());
        model.addAttribute("titulo", "Gestión de Salón y Mesas");
        return "modules/salon/mesas";
    }

    @PostMapping("/guardar")
    public String registrarEstructuraMesa(@ModelAttribute("mesaObj") Mesa mesa, RedirectAttributes flash) {
        try {
            if (mesa.getId() == null) {
                mesa.setEstado("LIBRE");
                mesa.setActivo(true);
                mesa.setEliminado(false);
            }
            mesaRepository.save(mesa);
            flash.addFlashAttribute("success", "Mesa procesada correctamente en el mapa del salón.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar la mesa: " + e.getMessage());
        }
        return "redirect:/mesas";
    }

    @GetMapping("/eliminar/{id}")
    public String darDeBajaMesa(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Mesa mesa = mesaRepository.findById(id).orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
            if ("OCUPADA".equals(mesa.getEstado())) {
                throw new RuntimeException("No se puede eliminar una mesa que tiene una comanda activa.");
            }
            mesaRepository.delete(mesa);
            flash.addFlashAttribute("success", "Mesa dada de baja del salón correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @GetMapping("/pedido/nuevo")
    public String formularioNuevoPedido(@RequestParam("mesaId") Long mesaId, Model model) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada."));

        if (!"LIBRE".equals(mesa.getEstado())) {
            return "redirect:/mesas";
        }

        model.addAttribute("mesa", mesa);
        model.addAttribute("productos", salonService.listarProductosDisponibles());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("titulo", "Toma de Pedido - Mesa #" + mesa.getNumero());
        return "modules/salon/nuevo-pedido";
    }

    @PostMapping("/pedido/confirmar")
    public String confirmarPedidoSalon(@RequestParam("mesaId") Long mesaId,
            @RequestParam("nombreCliente") String nombreCliente,
            @RequestParam("notasAdicionales") String notas,
            @RequestParam("productoIds") List<Long> productoIds,
            @RequestParam("cantidades") List<Integer> cantidades,
            RedirectAttributes flash) {
        try {
            salonService.registrarPedidoSalon(mesaId, nombreCliente, notas, productoIds, cantidades);
            flash.addFlashAttribute("success", "¡Comanda enviada a cocina de forma exitosa!");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @GetMapping("/pedido/agregar")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String vistaAgregarMasProductos(@RequestParam("pedidoId") Long pedidoId, Model model) {
        Pedido pedido = pedidoRepository.findWithDetailsById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        model.addAttribute("pedido", pedido);
        model.addAttribute("productos", salonService.listarProductosDisponibles());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("titulo", "Añadir productos a Comanda #" + pedidoId);
        return "modules/salon/agregar-productos";
    }

    @PostMapping("/agregar-producto")
    public String agregarProductoAComandaActiva(@RequestParam("pedidoId") Long pedidoId,
            @RequestParam("productoId") Long productoId,
            @RequestParam("cantidad") Integer cantidad,
            RedirectAttributes flash) {
        try {
            salonService.agregarProductoAPedido(pedidoId, productoId, cantidad);
            flash.addFlashAttribute("success", "Platillo adicionado a la orden correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/mesas/pedido/agregar?pedidoId=" + pedidoId;
    }

    @PostMapping("/pedido/cancelar")
    public String cancelarComandaMesa(@RequestParam("pedidoId") Long pedidoId, RedirectAttributes flash) {
        try {
            salonService.cancelarPedidoSalon(pedidoId);
            flash.addFlashAttribute("success", "Comanda cancelada y mesa liberada correctamente.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @PostMapping("/pedido/pagar")
    public String procesarPagoComandaMesa(@RequestParam("pedidoId") Long pedidoId,
            @RequestParam("metodoPago") String metodoPago,
            RedirectAttributes flash) {
        try {
            salonService.pagarPedidoSalon(pedidoId, metodoPago);
            flash.addFlashAttribute("success", "¡Mesa pagada con éxito! Registro enviado a reportes.");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

}
