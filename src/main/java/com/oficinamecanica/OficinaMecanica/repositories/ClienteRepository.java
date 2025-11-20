package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Listar apenas clientes ativos
    List<Cliente> findByAtivoTrue();

    // Listar clientes inativos
    List<Cliente> findByAtivoFalse();

    // Buscar por CPF
    Optional<Cliente> findByNuCPF(String nuCPF);

    // Buscar por email
    Optional<Cliente> findByEmail(String email);

    // Buscar por telefone
    List<Cliente> findByNuTelefone(String nuTelefone);

    // Buscar por nome (contém)
    List<Cliente> findByNmClienteContainingIgnoreCase(String nmCliente);

    // Verificar se CPF já existe
    boolean existsByNuCPF(String nuCPF);

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Buscar clientes com veículos
    @Query("SELECT DISTINCT c FROM Cliente c LEFT JOIN FETCH c.veiculos WHERE c.ativo = true")
    List<Cliente> findClientesComVeiculos();
}
