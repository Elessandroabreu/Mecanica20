// src/main/java/com/oficinamecanica/OficinaMecanica/repositories/VeiculoRepository.java

package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    // ✅ CORRIGIDO: Buscar todos com cliente carregado (EAGER) - SEM ativo
    @Query("SELECT v FROM Veiculo v LEFT JOIN FETCH v.cliente")
    List<Veiculo> findAllWithCliente();

    // ✅ CORRIGIDO: Buscar por ID com cliente carregado - SEM ativo
    @Query("SELECT v FROM Veiculo v LEFT JOIN FETCH v.cliente WHERE v.cdVeiculo = :id")
    Optional<Veiculo> findByIdWithCliente(Integer id);

    // Buscar por placa
    Optional<Veiculo> findByPlaca(String placa);

    // Verificar se placa já existe
    boolean existsByPlaca(String placa);

    // ✅ CORRIGIDO: Buscar veículos por cliente - SEM ativo
    List<Veiculo> findByClienteCdCliente(Integer cdCliente);
}