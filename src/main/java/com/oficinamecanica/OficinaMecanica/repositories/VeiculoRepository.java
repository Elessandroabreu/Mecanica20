package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    // Buscar por placa
    Optional<Veiculo> findByPlaca(String placa);

    // Listar veículos de um cliente
    List<Veiculo> findByCliente_CdCliente(Integer cdCliente);

    // Buscar por modelo
    List<Veiculo> findByModeloContainingIgnoreCase(String modelo);

    // Buscar por marca
    List<Veiculo> findByMarcaContainingIgnoreCase(String marca);

    // Buscar por ano
    List<Veiculo> findByAno(Integer ano);

    // Verificar se placa já existe
    boolean existsByPlaca(String placa);

    // Buscar veículo com histórico de ordens de serviço
    @Query("SELECT v FROM Veiculo v LEFT JOIN FETCH v.ordensServico WHERE v.cdVeiculo = :cdVeiculo")
    Optional<Veiculo> findByIdWithOrdensServico(@Param("cdVeiculo") Integer cdVeiculo);

    // Buscar veículos por cliente e listar com ordens
    @Query("SELECT DISTINCT v FROM Veiculo v LEFT JOIN FETCH v.ordensServico WHERE v.cliente.cdCliente = :cdCliente")
    List<Veiculo> findByClienteWithOrdensServico(@Param("cdCliente") Integer cdCliente);
}
