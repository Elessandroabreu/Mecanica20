package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.VeiculoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<VeiculoModel, Integer> {

    // Buscar todos com cliente carregado (EAGER)
    @Query("SELECT v FROM VeiculoModel v LEFT JOIN FETCH v.clienteModel")
    List<VeiculoModel> findAllWithCliente();

    // Buscar por ID com cliente carregado
    @Query("SELECT v FROM VeiculoModel v LEFT JOIN FETCH v.clienteModel WHERE v.cdVeiculo = :id")
    Optional<VeiculoModel> findByIdWithCliente(@Param("id") Integer id);

    // Buscar por placa
    Optional<VeiculoModel> findByPlaca(String placa);

    // Verificar se placa já existe
    boolean existsByPlaca(String placa);

    // CORRIGIDO: Model usa clienteModel, não cliente
    List<VeiculoModel> findByClienteModel_CdCliente(Integer cdCliente);
}