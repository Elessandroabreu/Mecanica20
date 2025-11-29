package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ItemOrdemServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServicoModel, Integer> {

    List<ItemOrdemServicoModel> findByOrdemServico_CdOrdemServico(Integer cdOrdemServico);
}