package com.Gestion.PolleriaLatina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Incidencia;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
  @Query("SELECT i FROM Incidencia i JOIN FETCH i.usuarioReporta")
  List<Incidencia> findAllWithUsuario();

  @Query("SELECT i FROM Incidencia i JOIN FETCH i.usuarioReporta ORDER BY i.fechaReporte DESC")
  List<Incidencia> findAllWithUsuarioOrderByFechaDesc();
}