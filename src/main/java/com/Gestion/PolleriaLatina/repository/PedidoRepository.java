package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

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

        @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto " +
                        "WHERE p.mesa.id = :mesaId AND p.modalidad = 'LOCAL' AND p.estado <> 'ENTREGADO'")
        Optional<Pedido> findActiveOrderByMesaId(@Param("mesaId") Long mesaId);

        @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto " +
                        "WHERE p.id = :pedidoId")
        Optional<Pedido> findWithDetailsById(@Param("pedidoId") Long pedidoId);

        @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.mesa " + 
           "JOIN FETCH p.detalles d " +
           "JOIN FETCH d.producto prod " +
           "LEFT JOIN FETCH prod.presentacion " + 
           "WHERE p.estado IN ('REGISTRADO', 'EN_PREPARACION') " +
           "ORDER BY p.fechaRegistro ASC")
    java.util.List<Pedido> findPedidosParaCocina();

}
