package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Insumo;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Long> {
  Optional<Insumo> findByNombre(String nombre);

  @Query("SELECT i FROM Insumo i JOIN FETCH i.unidadMedida")
  List<Insumo> findAllConUnidades();
}