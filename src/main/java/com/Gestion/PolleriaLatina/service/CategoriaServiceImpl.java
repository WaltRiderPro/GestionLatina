package com.Gestion.PolleriaLatina.service;

import org.springframework.stereotype.Service;
import com.Gestion.PolleriaLatina.model.Categoria;
import com.Gestion.PolleriaLatina.repository.CategoriaRepository;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La categoría con ID " + id + " no existe."));
    }

    @Override
    @Transactional
    public void guardar(Categoria categoria) {
        // Validaciones profesionales básicas: limpiar espacios en blanco
        categoria.setNombre(categoria.getNombre().trim());
        if (categoria.getDescripcion() != null) {
            categoria.setDescripcion(categoria.getDescripcion().trim());
        }
        
        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        // Al ejecutar delete, Hibernate ejecutará el @SQLDelete (soft delete) cambiando el estado a false
        categoriaRepository.delete(categoria);
    }

}
