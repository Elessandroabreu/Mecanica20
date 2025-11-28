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

    // Buscar por CPF - CORRIGIDO: Model usa CPF, não nuCPF
    Optional<ClienteModel> findByCpf(String cpf);

    // Verificar se CPF já existe - CORRIGIDO
    boolean existsByCpf(String cpf);

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Buscar por nome (busca parcial) - CORRIGIDO: Model usa nmCliente
    List<ClienteModel> findByNmClienteContainingIgnoreCase(String nmCliente);
}