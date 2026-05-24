package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.Gestion.PolleriaLatina.model.Producto;

public interface ProductoService {
    List<Producto> listarTodos();
    Producto buscarPorId(Long id);
    void guardar(Producto producto, List<MultipartFile> archivos);
    void eliminar(Long id);

}
