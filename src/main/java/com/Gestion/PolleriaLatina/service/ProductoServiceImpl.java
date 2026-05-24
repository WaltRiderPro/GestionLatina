package com.Gestion.PolleriaLatina.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.Gestion.PolleriaLatina.model.Producto;
import com.Gestion.PolleriaLatina.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final UploadFileService uploadFileService;
    private final ObjectMapper objectMapper; 

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findByEliminadoFalseOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El producto consultado no existe."));
    }

    @Override
    @Transactional
    public void guardar(Producto producto, List<MultipartFile> archivos) {
        producto.setNombre(producto.getNombre().trim());
        
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new RuntimeException("Error: El producto debe pertenecer obligatoriamente a una Categoría.");
        }
        if (producto.getPresentacion() == null || producto.getPresentacion().getId() == null) {
            throw new RuntimeException("Error: El producto debe tener una Presentación obligatoriamente.");
        }

        if (producto.getId() != null) {
            Producto productoExistente = buscarPorId(producto.getId());
            producto.setImagenesJson(productoExistente.getImagenesJson()); 
        }

        List<String> listaFotos = new ArrayList<>();
        boolean hayArchivosNuevos = false;

        if (archivos != null && !archivos.isEmpty()) {
            for (MultipartFile archivo : archivos) {
                if (archivo != null && !archivo.isEmpty()) {
                    String nombreFotoUnico = uploadFileService.guardarImagen(archivo);
                    listaFotos.add(nombreFotoUnico);
                    hayArchivosNuevos = true;
                }
            }
        }

        if (hayArchivosNuevos) {
            try {
                String jsonString = objectMapper.writeValueAsString(listaFotos);
                producto.setImagenesJson(jsonString);
            } catch (Exception e) {
                throw new RuntimeException("Fallo al estructurar el JSON de imágenes: " + e.getMessage());
            }
        }

        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setEliminado(true); 
        productoRepository.save(producto);
    }

}
