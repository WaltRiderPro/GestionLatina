package com.Gestion.PolleriaLatina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.KardexMovimiento;

@Repository
public interface KardexMovimientoRepository extends JpaRepository<KardexMovimiento, Long> {

  @Query("SELECT k FROM KardexMovimiento k " +
      "JOIN FETCH k.insumo i " +
      "JOIN FETCH i.unidadMedida " +
      "JOIN FETCH k.usuario " +
      "ORDER BY k.fechaRegistro DESC")
  List<KardexMovimiento> findAllCompleto();

  @Query("SELECT k FROM KardexMovimiento k " +
      "JOIN FETCH k.insumo i " +
      "JOIN FETCH i.unidadMedida " +
      "JOIN FETCH k.usuario " +
      "WHERE i.id = :insumoId " +
      "ORDER BY k.fechaRegistro DESC")
  List<KardexMovimiento> findByInsumoIdCompleto(@Param("insumoId") Long insumoId);
}