package com.Gestion.PolleriaLatina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.InventarioDetalle;

@Repository
public interface InventarioDetalleRepository extends JpaRepository<InventarioDetalle, Long> {
}