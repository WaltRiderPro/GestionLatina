package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Gestion.PolleriaLatina.model.Presentacion;
import com.Gestion.PolleriaLatina.repository.PresentacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresentacionServiceImpl implements PresentacionService{

    private final PresentacionRepository presentacionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Presentacion> listarTodas() {
        return presentacionRepository.findByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Presentacion buscarPorId(Long id) {
        return presentacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La presentación con ID " + id + " no existe."));
    }

    @Override
    @Transactional
    public void guardar(Presentacion presentacion) {
        if (presentacion.getNombre() != null) {
            presentacion.setNombre(presentacion.getNombre().trim());
        }
        presentacionRepository.save(presentacion);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Presentacion presentacion = buscarPorId(id);
        presentacionRepository.delete(presentacion);
    }

}
