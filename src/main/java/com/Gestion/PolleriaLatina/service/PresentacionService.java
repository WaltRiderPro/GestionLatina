package com.Gestion.PolleriaLatina.service;

import java.util.List;

import com.Gestion.PolleriaLatina.model.Presentacion;

public interface PresentacionService {
    List<Presentacion> listarTodas();
    Presentacion buscarPorId(Long id);
    void guardar(Presentacion presentacion);
    void eliminar(Long id);

}
