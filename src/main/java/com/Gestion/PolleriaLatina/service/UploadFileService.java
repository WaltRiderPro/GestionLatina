package com.Gestion.PolleriaLatina.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {
    String guardarImagen(MultipartFile archivo);
    void eliminarImagen(String nombreImagen);
}
