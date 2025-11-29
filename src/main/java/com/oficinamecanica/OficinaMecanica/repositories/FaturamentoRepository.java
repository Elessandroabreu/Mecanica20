package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.FaturamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FaturamentoRepository extends JpaRepository<FaturamentoModel, Integer> {

    @Query("SELECT f FROM FaturamentoModel f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    List<FaturamentoModel> findFaturamentosNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                                     @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT SUM(f.vlTotal) FROM FaturamentoModel f WHERE f.dataVenda BETWEEN :dataInicio AND :dataFim")
    Double calcularTotalFaturadoNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                          @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT f FROM FaturamentoModel f WHERE CAST(f.dataVenda AS date) = CAST(:data AS date)")
    List<FaturamentoModel> findFaturamentosDoDia(@Param("data") LocalDateTime data);

    @Query("SELECT SUM(f.vlTotal) FROM FaturamentoModel f WHERE CAST(f.dataVenda AS date) = CAST(:data AS date)")
    Double calcularTotalFaturadoDoDia(@Param("data") LocalDateTime data);

    FaturamentoModel findByOrdemServico_CdOrdemServico(Integer cdOrdemServico);

    List<FaturamentoModel> findByDataVendaBetween(LocalDateTime inicio, LocalDateTime fim);


}