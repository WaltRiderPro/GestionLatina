package com.Gestion.PolleriaLatina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Gestion.PolleriaLatina.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

  @Query("SELECT DISTINCT p FROM Pedido p " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "LEFT JOIN FETCH p.repartidor " +
      "WHERE p.modalidad = 'DELIVERY' " +
      "ORDER BY p.fechaRegistro DESC")
  List<Pedido> findByModalidadDeliveryCompleto();

  @Query("SELECT DISTINCT p FROM Pedido p " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "WHERE p.modalidad = 'DELIVERY' " +
      "AND p.estado = 'LISTO' " +
      "AND p.repartidor IS NULL " +
      "ORDER BY p.fechaRegistro ASC")
  List<Pedido> findPedidosListosParaDespachar();

  @Query("SELECT DISTINCT p FROM Pedido p " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "WHERE p.modalidad = 'DELIVERY' " +
      "AND p.repartidor.id = :repartidorId " +
      "AND p.estado = 'EN_RUTA' " +
      "ORDER BY p.fechaRegistro ASC")
  List<Pedido> findRutaActivaRepartidor(@Param("repartidorId") Long repartidorId);

  @Query("SELECT DISTINCT p FROM Pedido p " +
      "JOIN FETCH p.repartidor r " +
      "LEFT JOIN FETCH p.detalles d " +
      "LEFT JOIN FETCH d.producto " +
      "WHERE p.modalidad = 'DELIVERY' " +
      "AND p.estado = 'EN_RUTA' " +
      "ORDER BY p.fechaRegistro DESC")
  List<Pedido> findPedidosEnTransito();
}