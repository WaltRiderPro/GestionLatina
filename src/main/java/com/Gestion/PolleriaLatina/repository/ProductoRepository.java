package com.Gestion.PolleriaLatina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Gestion.PolleriaLatina.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.presentacion WHERE p.eliminado = false ORDER BY p.nombre ASC")
    List<Producto> findByEliminadoFalseOrderByNombreAsc();

}
