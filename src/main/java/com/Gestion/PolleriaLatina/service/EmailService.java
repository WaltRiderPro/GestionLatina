package com.Gestion.PolleriaLatina.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  public void enviarCorreoRecuperacion(String destino, String nombreUsuario, String enlaceRecuperacion) {
    SimpleMailMessage mensaje = new SimpleMailMessage();
    mensaje.setTo(destino);
    mensaje.setSubject("Recuperación de Contraseña - Pollería Latina");

    String contenido = "Hola " + nombreUsuario + ",\n\n"
        + "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.\n"
        + "Por favor, haz clic en el siguiente enlace para crear una nueva contraseña:\n\n"
        + enlaceRecuperacion + "\n\n"
        + "Si no solicitaste este cambio, ignora este correo.\n\n"
        + "Atentamente,\nEl equipo de Pollería Latina.";

    mensaje.setText(contenido);
    mailSender.send(mensaje);
  }
}