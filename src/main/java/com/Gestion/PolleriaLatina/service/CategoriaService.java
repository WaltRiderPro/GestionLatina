package com.Gestion.PolleriaLatina.service;

import java.util.List;
import com.Gestion.PolleriaLatina.model.Categoria;

public interface CategoriaService {
    List<Categoria> listarTodas();

    Categoria buscarPorId(Long id);

    void guardar(Categoria categoria);

    void eliminar(Long id);

}
