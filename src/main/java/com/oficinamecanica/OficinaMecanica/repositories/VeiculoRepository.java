package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    // Buscar por placa
    Optional<Veiculo> findByPlaca(String placa);

    // Listar veículos de um cliente
    List<Veiculo> findByCliente_CdCliente(Integer cdCliente);

    // Verificar se placa já existe
    boolean existsByPlaca(String placa);
}