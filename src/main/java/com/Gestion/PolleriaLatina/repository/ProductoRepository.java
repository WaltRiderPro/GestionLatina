package com.Gestion.PolleriaLatina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Gestion.PolleriaLatina.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

  @Query("SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.presentacion WHERE p.eliminado = false ORDER BY p.nombre ASC")
  List<Producto> findByEliminadoFalseOrderByNombreAsc();

  // NUEVO: Extraer un solo producto asegurando la carga de categoría y
  // presentación
  @Query("SELECT p FROM Producto p " +
      "LEFT JOIN FETCH p.categoria " +
      "LEFT JOIN FETCH p.presentacion " +
      "WHERE p.id = :id")
  Optional<Producto> findByIdConCategoriasYPresentaciones(@Param("id") Long id);

  @Query("SELECT p FROM Producto p " +
      "LEFT JOIN FETCH p.categoria " +
      "LEFT JOIN FETCH p.presentacion ")
  List<Producto> findAllConCategoriasYPresentaciones();

  @Query("SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.presentacion WHERE p.eliminado = false AND p.activo = true ORDER BY p.nombre ASC")
  List<Producto> findActiveProductsWithRelations();

}
