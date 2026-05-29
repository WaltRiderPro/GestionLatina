package com.Gestion.PolleriaLatina.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Gestion.PolleriaLatina.model.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findByActivoTrueAndEliminadoFalseOrderByNumeroAsc();

}
