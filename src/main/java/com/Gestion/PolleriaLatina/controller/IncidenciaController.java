package com.Gestion.PolleriaLatina.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Incidencia;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.IncidenciaRepository;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;
import com.Gestion.PolleriaLatina.service.IncidenciaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

  private final IncidenciaService incidenciaService;
  private final UsuarioRepository usuarioRepository;
  private final IncidenciaRepository incidenciaRepository;

  @GetMapping("/reportar")
  @PreAuthorize("hasAuthority('REPORTAR_INCIDENCIA_VER')")
  public String vistaReportarProblema(Model model) {
    model.addAttribute("incidencia", new Incidencia());
    model.addAttribute("titulo", "Centro de Soporte y Reportes");
    return "modules/incidencias/reportar";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAuthority('REPORTAR_INCIDENCIA_VER')")
  public String procesarReporte(
      @ModelAttribute Incidencia incidencia,
      @RequestParam(value = "files", required = false) List<MultipartFile> archivos,
      Authentication auth,
      RedirectAttributes flash) {
    try {
      Usuario usuario = usuarioRepository.findByUsername(auth.getName())
          .orElseThrow(() -> new RuntimeException("Usuario no identificado."));

      incidencia.setUsuarioReporta(usuario);
      incidenciaService.registrarIncidencia(incidencia, archivos);

      flash.addFlashAttribute("success",
          "El reporte ha sido enviado con éxito. El equipo de administración lo revisará a la brevedad.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Error al enviar el reporte: " + e.getMessage());
    }

    return "redirect:/incidencias/reportar";
  }

  @GetMapping("/gestion")
  @PreAuthorize("hasAuthority('GESTION_INCIDENCIAS_VER')")
  public String vistaGestionAdmin(Model model) {
    // Obtenemos la lista ya pasada por el algoritmo de inteligencia
    List<Incidencia> tickets = incidenciaService.listarIncidenciasPriorizadas();

    model.addAttribute("tickets", tickets);
    model.addAttribute("titulo", "Bandeja de Incidencias");
    return "modules/incidencias/gestion";
  }

  @PostMapping("/resolver")
  @PreAuthorize("hasAuthority('GESTION_INCIDENCIAS_VER')")
  public String resolverTicket(
      @RequestParam("incidenciaId") Long id,
      @RequestParam("respuestaAdmin") String respuesta,
      RedirectAttributes flash) {
    try {
      incidenciaService.resolverIncidencia(id, respuesta);
      flash.addFlashAttribute("success", "La incidencia ha sido marcada como RESUELTA.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Error al resolver: " + e.getMessage());
    }
    return "redirect:/incidencias/gestion";
  }

  @GetMapping("/historial")
  @PreAuthorize("hasAuthority('GESTION_INCIDENCIAS_VER')")
  public String vistaHistorialAdmin(Model model) {
    List<Incidencia> historial = incidenciaService.listarHistorialIncidencias();

    model.addAttribute("ticketsHistorial", historial);
    model.addAttribute("titulo", "Historial de Incidencias");
    return "modules/incidencias/historial";
  }
}