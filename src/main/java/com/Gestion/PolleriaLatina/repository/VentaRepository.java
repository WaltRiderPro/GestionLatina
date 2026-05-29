package com.Gestion.PolleriaLatina.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

  Optional<Venta> findByPedidoId(Long pedidoId);
}