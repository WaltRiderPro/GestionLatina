package com.Gestion.PolleriaLatina.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.Gestion.PolleriaLatina.model.Incidencia;
import com.Gestion.PolleriaLatina.repository.IncidenciaRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class IncidenciaServiceImpl implements IncidenciaService {

  private final IncidenciaRepository incidenciaRepository;
  private final UploadFileService uploadFileService;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void registrarIncidencia(Incidencia incidencia, List<MultipartFile> archivos) {

    incidencia.setEstado("PENDIENTE");
    incidencia.setFechaReporte(LocalDateTime.now());

    List<String> listaEvidencias = new ArrayList<>();

    if (archivos != null && !archivos.isEmpty()) {
      for (MultipartFile archivo : archivos) {
        if (archivo != null && !archivo.isEmpty()) {
          String nombreFotoUnico = uploadFileService.guardarImagen(archivo);
          listaEvidencias.add(nombreFotoUnico);
        }
      }
    }

    if (!listaEvidencias.isEmpty()) {
      try {
        String jsonString = objectMapper.writeValueAsString(listaEvidencias);
        incidencia.setEvidenciasJson(jsonString);
      } catch (Exception e) {
        throw new RuntimeException("Fallo al estructurar el JSON de evidencias: " + e.getMessage());
      }
    }

    incidenciaRepository.save(incidencia);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Incidencia> listarIncidenciasPriorizadas() {
    List<Incidencia> pendientes = incidenciaRepository.findAllWithUsuario();

    pendientes.removeIf(i -> "RESUELTO".equals(i.getEstado()) || "DESCARTADO".equals(i.getEstado()));

    pendientes.sort((a, b) -> {
      int gravA = getPuntajeGravedad(a.getNivelGravedad());
      int gravB = getPuntajeGravedad(b.getNivelGravedad());
      if (gravA != gravB) {
        return Integer.compare(gravB, gravA);
      }

      int modA = getPuntajeModulo(a.getTipo());
      int modB = getPuntajeModulo(b.getTipo());
      if (modA != modB) {
        return Integer.compare(modB, modA);
      }

      return a.getFechaReporte().compareTo(b.getFechaReporte());
    });

    return pendientes;
  }

  @Override
  @Transactional
  public void resolverIncidencia(Long id, String respuestaAdmin) {
    Incidencia incidencia = incidenciaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));
    incidencia.setEstado("RESUELTO");
    incidencia.setFechaResolucion(LocalDateTime.now());
    incidencia.setRespuestaAdmin(respuestaAdmin);
    incidenciaRepository.save(incidencia);
  }

  private int getPuntajeGravedad(String gravedad) {
    if (gravedad == null)
      return 0;
    return switch (gravedad.toUpperCase()) {
      case "EMERGENCIA" -> 3;
      case "MEDIO" -> 2;
      case "BAJO" -> 1;
      default -> 0;
    };
  }

  private int getPuntajeModulo(String tipo) {
    if (tipo == null)
      return 0;
    return switch (tipo.toUpperCase()) {
      case "SISTEMA_CAJA" -> 6;
      case "COCINA" -> 5;
      case "CLIENTE" -> 4;
      case "HARDWARE" -> 3;
      case "INFRAESTRUCTURA" -> 2;
      case "OTROS" -> 1;
      default -> 0;
    };
  }

  @Override
  @Transactional(readOnly = true)
  public List<Incidencia> listarHistorialIncidencias() {
    return incidenciaRepository.findAllWithUsuarioOrderByFechaDesc();
  }
}