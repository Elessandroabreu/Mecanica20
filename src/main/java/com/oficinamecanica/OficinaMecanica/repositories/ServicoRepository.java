package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoModel, Integer> {

    // Listar apenas serviços ativos
    List<ServicoModel> findByAtivoTrue();

        // Buscar por nome
    List<ServicoModel> findByNmServicoContainingIgnoreCase(String nmServico);

}
