package com.Gestion.PolleriaLatina.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.Gestion.PolleriaLatina.dto.ModuloPermisoDto;
import com.Gestion.PolleriaLatina.dto.RolWrapper;
import com.Gestion.PolleriaLatina.model.Permiso;
import com.Gestion.PolleriaLatina.model.Rol;
import com.Gestion.PolleriaLatina.model.RolPermiso;
import com.Gestion.PolleriaLatina.service.PermisoService;
import com.Gestion.PolleriaLatina.service.RolService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/roles")
@PreAuthorize("hasAuthority('ROLES_VER')")
@RequiredArgsConstructor
public class RolController {

  private final RolService rolService;
  private final PermisoService permisoService;

  @GetMapping
  @PreAuthorize("hasAuthority('ROLES_VER')")
  @Transactional(readOnly = true)
  public String listarRoles(Model model) {
    model.addAttribute("roles", rolService.listarRolesActivos());
    model.addAttribute("titulo", "Gestión de Roles y Permisos");
    return "modules/configuracion/roles";
  }

  @GetMapping("/nuevo")
  @PreAuthorize("hasAuthority('ROLES_CREAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioNuevo(Model model) {
    List<Permiso> todosLosPermisos = permisoService.listarTodos();

    List<ModuloPermisoDto> permisosDto = todosLosPermisos.stream()
        .map(p -> ModuloPermisoDto.builder()
            .permisoId(p.getId()).nombreModulo(p.getNombre()).agrupamiento(p.getModulo())
            .ver(false).crear(false).editar(false).eliminar(false).build())
        .collect(Collectors.toList());

    RolWrapper wrapper = new RolWrapper(new Rol(), permisosDto);

    model.addAttribute("wrapper", wrapper);
    model.addAttribute("titulo", "Registrar Nuevo Rol");
    return "modules/configuracion/rol-form";
  }

  @GetMapping("/editar/{id}")
  @PreAuthorize("hasAuthority('ROLES_EDITAR')")
  @Transactional(readOnly = true)
  public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes flash) {
    Rol rol = rolService.obtenerPorId(id);
    if (rol == null) {
      flash.addFlashAttribute("error", "El rol no existe.");
      return "redirect:/roles";
    }
    if ("ADMIN".equals(rol.getNombre())) {
      flash.addFlashAttribute("error", "SEGURIDAD: El rol ADMIN no puede ser editado.");
      return "redirect:/roles";
    }

    List<Permiso> todosLosPermisos = permisoService.listarTodos();
    Map<Long, RolPermiso> permisosActualesMap = rol.getPermisos().stream()
        .collect(Collectors.toMap(rp -> rp.getPermiso().getId(), rp -> rp));

    List<ModuloPermisoDto> permisosDto = new ArrayList<>();
    for (Permiso p : todosLosPermisos) {
      RolPermiso rp = permisosActualesMap.get(p.getId());
      ModuloPermisoDto dto = ModuloPermisoDto.builder()
          .permisoId(p.getId()).nombreModulo(p.getNombre()).agrupamiento(p.getModulo()).build();

      if (rp != null) {
        dto.setVer(rp.isPuedeVer());
        dto.setCrear(rp.isPuedeCrear());
        dto.setEditar(rp.isPuedeEditar());
        dto.setEliminar(rp.isPuedeEliminar());
      } else {
        dto.setVer(false);
        dto.setCrear(false);
        dto.setEditar(false);
        dto.setEliminar(false);
      }
      permisosDto.add(dto);
    }

    RolWrapper wrapper = new RolWrapper(rol, permisosDto);
    model.addAttribute("wrapper", wrapper);
    model.addAttribute("titulo", "Editar Rol: " + rol.getNombre());
    return "modules/configuracion/rol-form";
  }

  @GetMapping("/estado/{id}")
  @PreAuthorize("hasAuthority('ROLES_EDITAR')")
  public String cambiarEstadoRol(@PathVariable Long id, RedirectAttributes flash) {
    Rol rol = rolService.obtenerPorId(id);

    if (rol != null && "ADMIN".equals(rol.getNombre())) {
      flash.addFlashAttribute("error",
          "SEGURIDAD: No puedes desactivar el rol 'ADMIN'. Es vital para el funcionamiento del sistema.");
      return "redirect:/roles";
    }

    try {
      rolService.cambiarEstadoActivo(id);
      flash.addFlashAttribute("success", "El estado del rol ha sido actualizado.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/roles";
  }

  @PostMapping("/guardar")
  @PreAuthorize("hasAnyAuthority('ROLES_CREAR', 'ROLES_EDITAR')")
  public String guardarRol(@ModelAttribute("wrapper") RolWrapper wrapper, RedirectAttributes flash) {
    try {
      rolService.guardarCompleto(wrapper.getRol(), wrapper.getPermisos());
      flash.addFlashAttribute("success", "El Rol y sus Permisos se guardaron correctamente.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Ocurrió un error al guardar el Rol.");
    }
    return "redirect:/roles";
  }

  @GetMapping("/eliminar/{id}")
  @PreAuthorize("hasAuthority('ROLES_ELIMINAR')")
  public String eliminarRol(@PathVariable Long id, RedirectAttributes flash) {
    Rol rol = rolService.obtenerPorId(id);

    if (rol != null && "ADMIN".equals(rol.getNombre())) {
      flash.addFlashAttribute("error", "SEGURIDAD: El rol supremo 'ADMIN' no puede ser eliminado.");
      return "redirect:/roles";
    }

    try {
      rolService.eliminarLogico(id);
      flash.addFlashAttribute("success", "El rol ha sido eliminado correctamente.");
    } catch (RuntimeException e) {
      flash.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/roles";
  }
}