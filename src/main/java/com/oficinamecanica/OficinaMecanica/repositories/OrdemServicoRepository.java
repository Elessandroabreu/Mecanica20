package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.models.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    // Listar por status
    List<OrdemServico> findByStatus(Status status);

    // Listar ordens de um mecânico
    List<OrdemServico> findByMecanico_CdUsuario(Integer cdMecanico);

    // ✅ CORRIGIDO: Model usa clienteModel
    List<OrdemServico> findByClienteModel_CdCliente(Integer cdCliente);

    // Listar orçamentos pendentes (aguardando aprovação)
    @Query("SELECT o FROM OrdemServico o WHERE o.tipoOrdemOrcamento = 'ORCAMENTO' AND o.aprovado = false")
    List<OrdemServico> findOrcamentosPendentes();

    // Listar ordens com itens (fetch join) - IMPORTANTE para evitar N+1
    @Query("SELECT DISTINCT o FROM OrdemServico o LEFT JOIN FETCH o.itens WHERE o.cdOrdemServico = :cdOrdemServico")
    OrdemServico findByIdWithItens(@Param("cdOrdemServico") Integer cdOrdemServico);
}