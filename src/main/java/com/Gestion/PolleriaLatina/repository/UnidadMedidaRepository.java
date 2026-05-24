package com.Gestion.PolleriaLatina.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.UnidadMedida;

@Repository
public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
  Optional<UnidadMedida> findByNombre(String nombre);

  Optional<UnidadMedida> findByAbreviatura(String abreviatura);
}