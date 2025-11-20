package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Integer> {

    // Listar itens de uma venda
    List<ItemVenda> findByVenda_CdVenda(Integer cdVenda);

    // Listar itens que contêm um produto específico
    List<ItemVenda> findByProduto_CdProduto(Integer cdProduto);

    // Produtos mais vendidos em um período
    @Query("SELECT i.produto.nmProduto, SUM(i.quantidade) as total " +
           "FROM ItemVenda i " +
           "WHERE i.venda.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY i.produto.cdProduto, i.produto.nmProduto " +
           "ORDER BY total DESC")
    List<Object[]> findProdutosMaisVendidos(@Param("dataInicio") LocalDateTime dataInicio,
                                            @Param("dataFim") LocalDateTime dataFim);

    // Receita por produto em um período
    @Query("SELECT i.produto.nmProduto, SUM(i.vlTotal) as receita " +
           "FROM ItemVenda i " +
           "WHERE i.venda.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY i.produto.cdProduto, i.produto.nmProduto " +
           "ORDER BY receita DESC")
    List<Object[]> findReceitaPorProduto(@Param("dataInicio") LocalDateTime dataInicio,
                                         @Param("dataFim") LocalDateTime dataFim);
}
