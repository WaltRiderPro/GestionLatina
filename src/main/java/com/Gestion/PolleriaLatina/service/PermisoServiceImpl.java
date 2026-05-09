package com.Gestion.PolleriaLatina.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Gestion.PolleriaLatina.model.Permiso;
import com.Gestion.PolleriaLatina.repository.PermisoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {
  private final PermisoRepository permisoRepository;

  @Override
  public List<Permiso> listarTodos() {
    return permisoRepository.findAll();
  }
}