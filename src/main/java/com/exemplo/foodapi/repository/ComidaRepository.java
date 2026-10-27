package com.exemplo.foodapi.repository;

import com.exemplo.foodapi.entity.Comida;
import com.exemplo.foodapi.enums.CategoriaComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComidaRepository extends JpaRepository<Comida, Long> {
    List<Comida> findByCategoria(CategoriaComida categoria);

    Optional<Comida> findByNomeIgnoreCase(String nome);
}
