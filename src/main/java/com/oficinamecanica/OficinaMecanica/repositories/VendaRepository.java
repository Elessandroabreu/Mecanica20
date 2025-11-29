package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Integer> {

    // ✅ CRÍTICO: Busca todas as vendas com JOIN FETCH para evitar LazyInitializationException
    @Query("""
        SELECT DISTINCT v FROM Venda v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        ORDER BY v.dataVenda DESC
    """)
    List<Venda> findAllWithDetails();

    // ✅ ADICIONAL: Busca uma venda por ID com todos os relacionamentos
    @Query("""
        SELECT v FROM Venda v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE v.cdVenda = :cdVenda
    """)
    Optional<Venda> findByIdWithDetails(@Param("cdVenda") Integer cdVenda);

    // ✅ Listar vendas de um cliente
    List<Venda> findByClienteModel_CdCliente(Integer cdCliente);

    // ✅ Listar vendas de um atendente
    List<Venda> findByAtendente_CdUsuario(Integer cdAtendente);

    // ✅ Listar vendas em um período
    @Query("SELECT v FROM Venda v WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim ORDER BY v.dataVenda DESC")
    List<Venda> findVendasNoPeriodo(@Param("dataInicio") LocalDateTime dataInicio,
                                    @Param("dataFim") LocalDateTime dataFim);

    // ✅ Total de vendas do dia
    @Query("SELECT SUM(v.vlTotal) FROM Venda v WHERE CAST(v.dataVenda AS date) = CAST(:data AS date)")
    Double calcularTotalVendasDoDia(@Param("data") LocalDateTime data);

    // ✅ OPCIONAL: Vendas por cliente com detalhes
    @Query("""
        SELECT DISTINCT v FROM Venda v
        LEFT JOIN FETCH v.clienteModel c
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE c.cdCliente = :cdCliente
        ORDER BY v.dataVenda DESC
    """)
    List<Venda> findByClienteWithDetails(@Param("cdCliente") Integer cdCliente);

    // ✅ OPCIONAL: Vendas por atendente com detalhes
    @Query("""
        SELECT DISTINCT v FROM Venda v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente a
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE a.cdUsuario = :cdAtendente
        ORDER BY v.dataVenda DESC
    """)
    List<Venda> findByAtendenteWithDetails(@Param("cdAtendente") Integer cdAtendente);

    // ✅ OPCIONAL: Vendas por período com detalhes
    @Query("""
        SELECT DISTINCT v FROM Venda v
        LEFT JOIN FETCH v.clienteModel
        LEFT JOIN FETCH v.atendente
        LEFT JOIN FETCH v.itens i
        LEFT JOIN FETCH i.produto
        WHERE v.dataVenda BETWEEN :dataInicio AND :dataFim
        ORDER BY v.dataVenda DESC
    """)
    List<Venda> findVendasNoPeriodoWithDetails(@Param("dataInicio") LocalDateTime dataInicio,
                                               @Param("dataFim") LocalDateTime dataFim);
}