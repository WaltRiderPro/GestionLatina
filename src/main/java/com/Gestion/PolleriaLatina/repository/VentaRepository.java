package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

  Optional<Venta> findByPedidoId(Long pedidoId);

  @Query("SELECT DISTINCT v FROM Venta v " +
      "LEFT JOIN FETCH v.pedido p " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "LEFT JOIN FETCH p.mesa " +
      "LEFT JOIN FETCH v.cajero " +
      "ORDER BY v.fechaEmision DESC")
  List<Venta> findAllConPedidoCompletoOrderByFechaEmisionDesc();

  @Query("SELECT DISTINCT v FROM Venta v " +
      "LEFT JOIN FETCH v.pedido p " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "LEFT JOIN FETCH p.mesa " +
      "LEFT JOIN FETCH v.cajero " +
      "WHERE v.id = :id")
  Optional<Venta> findConPedidoCompletoById(@Param("id") Long id);
}
