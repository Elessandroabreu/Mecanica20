package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.models.OrdemServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Integer> {

    List<OrdemServicoModel> findByStatus(Status status);

    List<OrdemServicoModel> findByMecanico_CdUsuario(Integer cdMecanico);

    List<OrdemServicoModel> findByClienteModel_CdCliente(Integer cdCliente);

    @Query("SELECT o FROM OrdemServicoModel o WHERE o.tipoOrdemOrcamento = 'ORCAMENTO' AND o.aprovado = false")
    List<OrdemServicoModel> findOrcamentosPendentes();

    @Query("SELECT DISTINCT o FROM OrdemServicoModel o " +
            "LEFT JOIN FETCH o.clienteModel " +
            "LEFT JOIN FETCH o.veiculo " +
            "LEFT JOIN FETCH o.mecanico " +
            "LEFT JOIN FETCH o.itens i " +
            "LEFT JOIN FETCH i.produto " +
            "LEFT JOIN FETCH i.servico " +
            "WHERE o.cdOrdemServico = :id")
    OrdemServicoModel findByIdWithItens(@Param("id") Integer id);

    // =============================
    // 🚀 FETCH BÁSICO PARA LISTAGEM
    // =============================
    @Query("SELECT DISTINCT o FROM OrdemServicoModel o " +
            "LEFT JOIN FETCH o.clienteModel " +
            "LEFT JOIN FETCH o.veiculo " +
            "LEFT JOIN FETCH o.mecanico")
    List<OrdemServicoModel> findAllWithBasicRelations();
}
