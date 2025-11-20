package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.StatusOrdemServico;
import com.oficinamecanica.OficinaMecanica.enums.TipoServico;
import com.oficinamecanica.OficinaMecanica.models.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    // Listar por status
    List<OrdemServico> findByStatusOrdemServico(StatusOrdemServico status);

    // Listar por tipo (ORCAMENTO ou ORDEM_DE_SERVICO)
    List<OrdemServico> findByTipoServico(TipoServico tipoServico);

    // Listar ordens de um cliente
    List<OrdemServico> findByCliente_CdCliente(Integer cdCliente);

    // Listar ordens de um veículo
    List<OrdemServico> findByVeiculo_CdVeiculo(Integer cdVeiculo);

    // Listar ordens de um mecânico
    List<OrdemServico> findByMecanico_CdUsuario(Integer cdMecanico);

    // Listar orçamentos pendentes (aguardando aprovação)
    @Query("SELECT o FROM OrdemServico o WHERE o.tipoServico = 'ORCAMENTO' AND o.aprovado = false")
    List<OrdemServico> findOrcamentosPendentes();

    // Listar orçamentos aprovados
    @Query("SELECT o FROM OrdemServico o WHERE o.tipoServico = 'ORCAMENTO' AND o.aprovado = true")
    List<OrdemServico> findOrcamentosAprovados();

    // Listar ordens em andamento de um mecânico
    @Query("SELECT o FROM OrdemServico o WHERE o.mecanico.cdUsuario = :cdMecanico " +
           "AND o.statusOrdemServico = 'EM_ANDAMENTO'")
    List<OrdemServico> findOrdensEmAndamentoPorMecanico(@Param("cdMecanico") Integer cdMecanico);

    // Listar ordens concluídas em um período
    @Query("SELECT o FROM OrdemServico o WHERE o.statusOrdemServico = 'CONCLUIDA' " +
           "AND o.dataFechamento BETWEEN :dataInicio AND :dataFim")
    List<OrdemServico> findOrdensConcluidasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                                      @Param("dataFim") LocalDateTime dataFim);

    // Calcular total de vendas em um período
    @Query("SELECT SUM(o.vlTotal) FROM OrdemServico o WHERE o.statusOrdemServico = 'CONCLUIDA' " +
           "AND o.dataFechamento BETWEEN :dataInicio AND :dataFim")
    Double calcularTotalVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                        @Param("dataFim") LocalDateTime dataFim);

    // Listar ordens com itens (fetch join)
    @Query("SELECT DISTINCT o FROM OrdemServico o LEFT JOIN FETCH o.itens WHERE o.cdOrdemServico = :cdOrdemServico")
    OrdemServico findByIdWithItens(@Param("cdOrdemServico") Integer cdOrdemServico);

    // Produtividade por mecânico (quantidade de ordens concluídas)
    @Query("SELECT COUNT(o) FROM OrdemServico o WHERE o.mecanico.cdUsuario = :cdMecanico " +
           "AND o.statusOrdemServico = 'CONCLUIDA' " +
           "AND o.dataFechamento BETWEEN :dataInicio AND :dataFim")
    Long countOrdensConcluidasPorMecanico(@Param("cdMecanico") Integer cdMecanico,
                                          @Param("dataInicio") LocalDateTime dataInicio,
                                          @Param("dataFim") LocalDateTime dataFim);
}
