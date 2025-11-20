package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

    // Listar vendas de um cliente
    List<Venda> findByCliente_CdCliente(Integer cdCliente);

    // Listar vendas de um atendente
    List<Venda> findByAtendente_CdUsuario(Integer cdAtendente);

    // Listar vendas por forma de pagamento
    List<Venda> findByFormaPagamento(Venda.FormaPagamento formaPagamento);

    // Listar vendas em um período
    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim")
    List<Venda> findVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                     @Param("dataFim") LocalDateTime dataFim);

    // Calcular total de vendas em um período
    @Query("SELECT SUM(v.vlTotal) FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim")
    Double calcularTotalVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                        @Param("dataFim") LocalDateTime dataFim);

    // Vendas do dia
    @Query("SELECT v FROM Venda v WHERE DATE(v.dataVenda) = DATE(:data)")
    List<Venda> findVendasDoDia(@Param("data") LocalDateTime data);

    // Total de vendas do dia
    @Query("SELECT SUM(v.vlTotal) FROM Venda v WHERE DATE(v.dataVenda) = DATE(:data)")
    Double calcularTotalVendasDoDia(@Param("data") LocalDateTime data);

    // Vendas com itens (fetch join)
    @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.cdVenda = :cdVenda")
    Venda findByIdWithItens(@Param("cdVenda") Integer cdVenda);

    // Ranking de atendentes por valor de vendas
    @Query("SELECT v.atendente.nmUsuario, SUM(v.vlTotal) as total " +
           "FROM Venda v " +
           "WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY v.atendente.cdUsuario, v.atendente.nmUsuario " +
           "ORDER BY total DESC")
    List<Object[]> findRankingAtendentesPorVendas(@Param("dataInicio") LocalDateTime dataInicio,
                                                   @Param("dataFim") LocalDateTime dataFim);
}
