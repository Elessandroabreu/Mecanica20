package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteModel, Integer> {

    // Listar apenas clientes ativos
    List<ClienteModel> findByAtivoTrue();

    // Buscar por CPF
    Optional<ClienteModel> findByNuCPF(String nuCPF);

    // Verificar se CPF já existe
    boolean existsByNuCPF(String nuCPF);

    // Verificar se email já existe
    boolean existsByEmail(String email);
}