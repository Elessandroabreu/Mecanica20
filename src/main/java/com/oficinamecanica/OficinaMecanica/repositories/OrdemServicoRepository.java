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

    // Listar por status
    List<OrdemServicoModel> findByStatus(Status status);

    // Listar ordens de um mecânico
    List<OrdemServicoModel> findByMecanico_CdUsuario(Integer cdMecanico);

    // ✅ CORRIGIDO: Model usa clienteModel
    List<OrdemServicoModel> findByClienteModel_CdCliente(Integer cdCliente);

    // Listar orçamentos pendentes (aguardando aprovação)
    @Query("SELECT o FROM OrdemServicoModel o WHERE o.tipoOrdemOrcamento = 'ORCAMENTO' AND o.aprovado = false")
    List<OrdemServicoModel> findOrcamentosPendentes();

//    // Listar ordens com itens (fetch join) - IMPORTANTE para evitar N+1
//    @Query("SELECT DISTINCT o FROM OrdemServicoModel o LEFT JOIN FETCH o.itens WHERE o.cdOrdemServico = :cdOrdemServico")
//    OrdemServicoModel findByIdWithItens(@Param("cdOrdemServico") Integer cdOrdemServico);
//}

@Query("SELECT DISTINCT o FROM OrdemServicoModel o " +
        "LEFT JOIN FETCH o.clienteModel " +
        "LEFT JOIN FETCH o.veiculo " +
        "LEFT JOIN FETCH o.mecanico " +
        "LEFT JOIN FETCH o.itens i " +
        "LEFT JOIN FETCH i.produto " +
        "LEFT JOIN FETCH i.servico " +
        "WHERE o.cdOrdemServico = :id")
OrdemServicoModel findByIdWithItens(@Param("id") Integer id);}