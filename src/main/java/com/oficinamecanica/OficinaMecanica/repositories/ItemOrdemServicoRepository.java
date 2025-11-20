package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ItemOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemOrdemServicoRepository extends JpaRepository<ItemOrdemServico, Integer> {

    // Listar itens de uma ordem de serviço
    List<ItemOrdemServico> findByOrdemServico_CdOrdemServico(Integer cdOrdemServico);

    // Listar itens que contêm um produto específico
    List<ItemOrdemServico> findByProduto_CdProduto(Integer cdProduto);

    // Listar itens que contêm um serviço específico
    List<ItemOrdemServico> findByServico_CdServico(Integer cdServico);

    // Produtos mais utilizados em um período
    @Query("SELECT i.produto.nmProduto, SUM(i.quantidade) as total " +
           "FROM ItemOrdemServico i " +
           "WHERE i.produto IS NOT NULL " +
           "AND i.ordemServico.dataAbertura BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY i.produto.cdProduto, i.produto.nmProduto " +
           "ORDER BY total DESC")
    List<Object[]> findProdutosMaisUtilizados(@Param("dataInicio") LocalDateTime dataInicio,
                                               @Param("dataFim") LocalDateTime dataFim);

    // Serviços mais executados em um período
    @Query("SELECT i.servico.nmServico, COUNT(i) as total " +
           "FROM ItemOrdemServico i " +
           "WHERE i.servico IS NOT NULL " +
           "AND i.ordemServico.dataAbertura BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY i.servico.cdServico, i.servico.nmServico " +
           "ORDER BY total DESC")
    List<Object[]> findServicosMaisExecutados(@Param("dataInicio") LocalDateTime dataInicio,
                                               @Param("dataFim") LocalDateTime dataFim);
}
