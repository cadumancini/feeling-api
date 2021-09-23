package com.br.feelingestofados.feelingapi.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface E070EMPRepository extends JpaRepository<E070EMP, Long> {
    @Query(value = "SELECT CODEMP, NOMEMP FROM E070EMP", nativeQuery = true)
    List<E070EMP> findAll();
}