package com.Gestion.PolleriaLatina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.InventarioCabecera;

@Repository
public interface InventarioCabeceraRepository extends JpaRepository<InventarioCabecera, Long> {
  java.util.List<InventarioCabecera> findAllByOrderByFechaInventarioDesc();
}