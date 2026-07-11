package com.Gestion.PolleriaLatina.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Gestion.PolleriaLatina.model.Usuario;
import com.Gestion.PolleriaLatina.repository.UsuarioRepository;
import com.Gestion.PolleriaLatina.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

  // Inyección de dependencias
  private final UsuarioRepository usuarioRepository;
  private final EmailService emailService;
  private final PasswordEncoder passwordEncoder;

  @GetMapping("/login")
  public String loginPage() {
    return "Auth/login";
  }

  @GetMapping("/forgot-password")
  public String forgotPasswordPage() {
    return "Auth/forgot-password";
  }

  @PostMapping("/forgot-password")
  public String processForgotPassword(
      @RequestParam("username") String username,
      @RequestParam("dni") String dni,
      @RequestParam("email") String email,
      HttpServletRequest request,
      RedirectAttributes flash) {

    Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameAndDniAndCorreoAndEliminadoFalse(username, dni,
        email);

    if (usuarioOpt.isEmpty()) {
      flash.addFlashAttribute("error", "Los datos ingresados no coinciden con ningún usuario activo en el sistema.");
      return "redirect:/forgot-password";
    }

    Usuario usuario = usuarioOpt.get();

    String token = UUID.randomUUID().toString();
    usuario.setResetToken(token);
    usuarioRepository.save(usuario);

    String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    String resetUrl = appUrl + "/reset-password?token=" + token;

    try {
      emailService.enviarCorreoRecuperacion(usuario.getCorreo(), usuario.getNombre(), resetUrl);
      flash.addFlashAttribute("success", "Se ha enviado un enlace de recuperación a tu correo electrónico.");
    } catch (Exception e) {
      flash.addFlashAttribute("error", "Hubo un error al enviar el correo. Por favor, contacta con el administrador.");
    }

    return "redirect:/forgot-password";
  }

  @GetMapping("/reset-password")
  public String resetPasswordPage(@RequestParam("token") String token, Model model, RedirectAttributes flash) {
    Optional<Usuario> usuarioOpt = usuarioRepository.findByResetTokenAndEliminadoFalse(token);

    if (usuarioOpt.isEmpty()) {
      flash.addFlashAttribute("error", "El enlace de recuperación es inválido o ya ha sido utilizado.");
      return "redirect:/login";
    }

    model.addAttribute("token", token);
    return "Auth/reset-password";
  }

  @PostMapping("/reset-password")
  public String processResetPassword(
      @RequestParam("token") String token,
      @RequestParam("password") String newPassword,
      RedirectAttributes flash) {

    Optional<Usuario> usuarioOpt = usuarioRepository.findByResetTokenAndEliminadoFalse(token);

    if (usuarioOpt.isEmpty()) {
      flash.addFlashAttribute("error", "El enlace de recuperación es inválido o ya ha sido utilizado.");
      return "redirect:/login";
    }

    Usuario usuario = usuarioOpt.get();

    usuario.setPassword(passwordEncoder.encode(newPassword));
    usuario.setResetToken(null);
    usuarioRepository.save(usuario);

    flash.addFlashAttribute("success", "¡Tu contraseña ha sido actualizada con éxito! Ya puedes iniciar sesión.");
    return "redirect:/login";
  }
}