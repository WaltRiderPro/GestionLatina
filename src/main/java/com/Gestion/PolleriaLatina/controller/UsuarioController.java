package com.Gestion.PolleriaLatina.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.service.RolService;
import com.Gestion.PolleriaLatina.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasAuthority('USUARIOS_VER')")
@RequiredArgsConstructor
public class UsuarioController {

  private final UsuarioService usuarioService;
  private final RolService rolService;

  @GetMapping
  @PreAuthorize("hasAuthority('USUARIOS_VER')")
  @Transactional(readOnly = true)
  public String listarUsuarios(Model model) {
    model.addAttribute("usuarios", usuarioService.listarUsuariosActivos());
    model.addAttribute("titulo", "Gestión de Usuarios");
    return "modules/configuracion/usuarios";
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasAuthority('USUARIOS_CREAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioNuevo(Model model) {
    List<Rol> rolesDisponibles = rolService.listarRolesActivos().stream()
        .filter(r -> !"ADMIN".equals(r.getNombre()))
        .collect(Collectors.toList());

    model.addAttribute("usuario", new Usuario());
    model.addAttribute("roles", rolesDisponibles);
    model.addAttribute("titulo", "Registrar Nuevo Usuario");
    return "modules/configuracion/usuario-form";
  }

  @GetMapping("/editar/{id}")
  @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes flash,
      Authentication auth) {
    Usuario usuario = usuarioService.obtenerPorId(id);

    if (usuario == null) {
      flash.addFlashAttribute("error", "El usuario no existe.");
      return "redirect:/usuarios";
    }

    if ("ADMIN".equals(usuario.getRol().getNombre()) && !usuario.getUsername().equals(auth.getName())) {
      flash.addFlashAttribute("error", "SEGURIDAD: No tienes autorización para editar a otro Administrador.");
      return "redirect:/usuarios";
    }

    List<Rol> rolesDisponibles;
    if ("ADMIN".equals(usuario.getRol().getNombre())) {

      rolesDisponibles = List.of(usuario.getRol());
    } else {
      rolesDisponibles = rolService.listarRolesActivos().stream()
          .filter(r -> !"ADMIN".equals(r.getNombre()))
          .collect(Collectors.toList());
    }

    model.addAttribute("usuario", usuario);
    model.addAttribute("roles", rolesDisponibles);
    model.addAttribute("titulo", "Editar Usuario: " + usuario.getUsername());
    return "modules/configuracion/usuario-form";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAnyAuthority('USUARIOS_CREAR', 'USUARIOS_EDITAR')")
  public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes flash, Model model) {
    try {
      usuarioService.guardarUsuario(usuario);
      flash.addFlashAttribute("success", "El usuario se ha guardado correctamente.");
      return "redirect:/usuarios";
    } catch (RuntimeException e) {

      model.addAttribute("error", e.getMessage());
      model.addAttribute("roles", rolService.listarRolesActivos());
      model.addAttribute("titulo",
          usuario.getId() == null ? "Registrar Nuevo Usuario" : "Editar Usuario: " + usuario.getUsername());
      return "modules/configuracion/usuario-form";
    }
  }

  @GetMapping("/estado/{id}")
  @PreAuthorize("hasAuthority('USUARIOS_EDITAR')")
  public String cambiarEstadoUsuario(@PathVariable Long id, RedirectAttributes flash, Authentication auth) {
    Usuario usuario = usuarioService.obtenerPorId(id);

    if (usuario != null && usuario.getUsername().equals(auth.getName())) {
      flash.addFlashAttribute("error", "No puedes desactivar tu propia cuenta mientras la estás usando.");
      return "redirect:/usuarios";
    }

    if (usuario != null && "admin".equals(usuario.getUsername())) {
      flash.addFlashAttribute("error", "El usuario maestro 'admin' no puede ser desactivado.");
      return "redirect:/usuarios";
    }

    try {
      usuarioService.cambiarEstadoActivo(id);
      flash.addFlashAttribute("success", "El estado de acceso del usuario ha sido actualizado.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/usuarios";
  }

  @GetMapping("/eliminar/{id}")
  @PreAuthorize("hasAuthority('USUARIOS_ELIMINAR')")
  public String eliminarUsuario(@PathVariable Long id, RedirectAttributes flash, Authentication auth) {
    Usuario usuario = usuarioService.obtenerPorId(id);

    if (usuario != null && usuario.getUsername().equals(auth.getName())) {
      flash.addFlashAttribute("error", "No puedes auto-eliminar tu cuenta en sesión.");
      return "redirect:/usuarios";
    }

    if (usuario != null && "admin".equals(usuario.getUsername())) {
      flash.addFlashAttribute("error", "SEGURIDAD: El usuario maestro 'admin' no puede ser eliminado.");
      return "redirect:/usuarios";
    }

    try {
      usuarioService.eliminarLogico(id);
      flash.addFlashAttribute("success", "Usuario eliminado correctamente del sistema.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/usuarios";
  }
}