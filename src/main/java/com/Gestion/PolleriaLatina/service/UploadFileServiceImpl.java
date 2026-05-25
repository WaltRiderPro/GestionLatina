package com.Gestion.PolleriaLatina.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileServiceImpl implements UploadFileService {

    private final String DIRECTORIO_PRODUCTOS = "uploads/productos";
    private final List<String> MIME_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    @Override
    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        String tipoMime = archivo.getContentType();
        if (tipoMime == null || !MIME_PERMITIDOS.contains(tipoMime)) {
            throw new RuntimeException("Formato no válido. Solo se admiten fotos de tipo JPG, PNG o WEBP.");
        }

        try {
            Path rutaDirectorio = Paths.get(DIRECTORIO_PRODUCTOS).toAbsolutePath().normalize();
            Files.createDirectories(rutaDirectorio);

            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal != null && nombreOriginal.contains(".")
                    ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                    : ".jpg";
            String nuevoNombre = UUID.randomUUID().toString() + extension;

            Path rutaDestino = rutaDirectorio.resolve(nuevoNombre);
            Files.copy(archivo.getInputStream(), rutaDestino);

            return nuevoNombre; 
        } catch (IOException e) {
            throw new RuntimeException("Fallo al guardar la imagen en el servidor: " + e.getMessage());
        }
    }

    @Override
    public void eliminarImagen(String nombreImagen) {
        if (nombreImagen != null && !nombreImagen.isEmpty()) {
            try {
                Path rutaArchivo = Paths.get(DIRECTORIO_PRODUCTOS).resolve(nombreImagen).toAbsolutePath().normalize();
                Files.deleteIfExists(rutaArchivo);
            } catch (IOException e) {
                System.err.println("No se pudo borrar el archivo físico antiguo: " + e.getMessage());
            }
        }
    }
}
