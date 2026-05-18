package com.Gestion.PolleriaLatina.repository;

import com.Gestion.PolleriaLatina.model.Categoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByActivoTrue();
}
