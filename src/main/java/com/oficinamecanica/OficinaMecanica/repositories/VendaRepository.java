package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.VendaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<VendaModel, Integer> {


    @Query("""
        SELECT DISTINCT v FROM VendaModel v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        ORDER BY v.dataVenda DESC
    """)
    List<VendaModel> findAllWithDetails();

    @Query("""
        SELECT v FROM VendaModel v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE v.cdVenda = :cdVenda
    """)
    Optional<VendaModel> findByIdWithDetails(@Param("cdVenda") Integer cdVenda);

    List<VendaModel> findByClienteModel_CdCliente(Integer cdCliente);

    List<VendaModel> findByAtendente_CdUsuario(Integer cdAtendente);

    @Query("SELECT v FROM VendaModel v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim ORDER BY v.dataVenda DESC")
    List<VendaModel> findVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                         @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT SUM(v.vlTotal) FROM VendaModel v WHERE CAST(v.dataVenda AS date) = CAST(:data AS date)")
    Double calcularTotalVendasDoDia(@Param("data") LocalDateTime data);

    @Query("""
        SELECT DISTINCT v FROM VendaModel v
        LEFT JOIN FETCH v.clienteModel c
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE c.cdCliente = :cdCliente
        ORDER BY v.dataVenda DESC
    """)
    List<VendaModel> findByClienteWithDetails(@Param("cdCliente") Integer cdCliente);

    @Query("""
        SELECT DISTINCT v FROM VendaModel v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente a
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE a.cdUsuario = :cdAtendente
        ORDER BY v.dataVenda DESC
    """)
    List<VendaModel> findByAtendenteWithDetails(@Param("cdAtendente") Integer cdAtendente);

    @Query("""
        SELECT DISTINCT v FROM VendaModel v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim
        ORDER BY v.dataVenda DESC
    """)
    List<VendaModel> findVendasNoPeriodoWithDetails(@Param("dataInicio") LocalDateTime dataInicio,
                                                    @Param("dataFim") LocalDateTime dataFim);
}