package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Receta;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {

  @Query("SELECT r FROM Receta r " +
      "JOIN FETCH r.producto p " +
      "JOIN FETCH r.insumo i " +
      "JOIN FETCH i.unidadMedida " +
      "WHERE p.id = :productoId")
  List<Receta> findByProductoIdCompleto(@Param("productoId") Long productoId);

  Optional<Receta> findByProductoIdAndInsumoId(Long productoId, Long insumoId);
}