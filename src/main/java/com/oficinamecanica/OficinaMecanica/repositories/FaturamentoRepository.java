package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.FormaPagamento;
import com.oficinamecanica.OficinaMecanica.models.Faturamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, Integer> {

    // Buscar faturamento por venda
    Optional<Faturamento> findByVenda_CdVenda(Integer cdVenda);

    // Buscar faturamento por ordem de serviço
    Optional<Faturamento> findByOrdemServico_CdOrdemServico(Integer cdOrdemServico);

    // Listar faturamentos por forma de pagamento
    List<Faturamento> findByFormaPagamento(FormaPagamento formaPagamento);

    // Listar faturamentos em um período
    @Query("SELECT f FROM Faturamento f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    List<Faturamento> findFaturamentosNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                                 @Param("dataFim") LocalDateTime dataFim);

    // Calcular total faturado em um período
    @Query("SELECT SUM(f.vlTotal) FROM Faturamento f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    Double calcularTotalFaturadoNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                          @Param("dataFim") LocalDateTime dataFim);

    // Faturamento do dia
    @Query("SELECT f FROM Faturamento f WHERE DATE(f.dataVenda) = DATE(:data)")
    List<Faturamento> findFaturamentosDoDia(@Param("data") LocalDateTime data);

    // Total faturado do dia
    @Query("SELECT SUM(f.vlTotal) FROM Faturamento f WHERE DATE(f.dataVenda) = DATE(:data)")
    Double calcularTotalFaturadoDoDia(@Param("data") LocalDateTime data);

    // Faturamento por forma de pagamento em um período
    @Query("SELECT f.formaPagamento, SUM(f.vlTotal) as total " +
           "FROM Faturamento f " +
           "WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY f.formaPagamento " +
           "ORDER BY total DESC")
    List<Object[]> findFaturamentoPorFormaPagamento(@Param("dataInicio") LocalDateTime dataInicio,
                                                     @Param("dataFim") LocalDateTime dataFim);
}
