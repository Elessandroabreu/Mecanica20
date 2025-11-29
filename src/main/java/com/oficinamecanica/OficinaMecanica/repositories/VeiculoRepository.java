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

    @Query("SELECT v FROM VeiculoModel v LEFT JOIN FETCH v.clienteModel")
    List<VeiculoModel> findAllWithCliente();

    @Query("SELECT v FROM VeiculoModel v LEFT JOIN FETCH v.clienteModel WHERE v.cdVeiculo = :id")
    Optional<VeiculoModel> findByIdWithCliente(@Param("id") Integer id);

    Optional<VeiculoModel> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    List<VeiculoModel> findByClienteModel_CdCliente(Integer cdCliente);
}