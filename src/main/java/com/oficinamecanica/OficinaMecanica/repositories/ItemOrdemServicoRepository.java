package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Integer> {

    // Listar itens de uma ordem de serviço
    List<ItemOrdemServico> findByOrdemServico_CdOrdemServico(Integer cdOrdemServico);
}