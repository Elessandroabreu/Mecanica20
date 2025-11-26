package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Faturamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, Integer> {

    // Listar faturamentos em um período
    @Query("SELECT f FROM Faturamento f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    List<Faturamento> findFaturamentosNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                                @Param("dataFim") LocalDateTime dataFim);

    // Calcular total faturado em um período
    @Query("SELECT SUM(f.vlTotal) FROM Faturamento f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    Double calcularTotalFaturadoNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                          @Param("dataFim") LocalDateTime dataFim);

    // Faturamento do dia (usando DATE para comparação correta)
    @Query("SELECT f FROM Faturamento f WHERE CAST(f.dataVenda AS date) = CAST(:data AS date)")
    List<Faturamento> findFaturamentosDoDia(@Param("data") LocalDateTime data);

    // Total faturado do dia
    @Query("SELECT SUM(f.vlTotal) FROM Faturamento f WHERE CAST(f.dataVenda AS date) = CAST(:data AS date)")
    Double calcularTotalFaturadoDoDia(@Param("data") LocalDateTime data);
}