package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.Gestion.PolleriaLatina.model.Incidencia;

public interface IncidenciaService {
  void registrarIncidencia(Incidencia incidencia, List<MultipartFile> archivos);

  List<Incidencia> listarIncidenciasPriorizadas();

  void resolverIncidencia(Long id, String respuestaAdmin);

  List<Incidencia> listarHistorialIncidencias();
}