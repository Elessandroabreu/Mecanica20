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

    // Listar vendas em um período
    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim")
    List<Venda> findVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                    @Param("dataFim") LocalDateTime dataFim);

    // Total de vendas do dia
    @Query("SELECT SUM(v.vlTotal) FROM Venda v WHERE CAST(v.dataVenda AS date) = CAST(:data AS date)")
    Double calcularTotalVendasDoDia(@Param("data") LocalDateTime data);
}