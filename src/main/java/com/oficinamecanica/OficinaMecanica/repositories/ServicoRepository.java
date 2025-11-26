package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    // Listar apenas serviços ativos
    List<Servico> findByAtivoTrue();

        // Buscar por nome
    List<Servico> findByNmServicoContainingIgnoreCase(String nmServico);

}
