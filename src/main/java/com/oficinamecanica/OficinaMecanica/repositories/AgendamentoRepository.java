package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import com.oficinamecanica.OficinaMecanica.models.AgendamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, Integer> {

    List<AgendamentoModel> findByStatus(Status status);

    List<AgendamentoModel> findByMecanico_CdUsuario(Integer cdMecanico);

    List<AgendamentoModel> findByCliente_CdCliente(Integer cdCliente);

    @Query("SELECT a FROM AgendamentoModel a WHERE a.dataAgendamento >= :dataAtual AND a.status = 'AGENDADO'")
    List<AgendamentoModel> findAgendamentosFuturos(@Param("dataAtual") LocalDate dataAtual);

    // ✅ Buscar agendamentos do mecânico em uma data (exceto cancelados)
    @Query("SELECT a FROM AgendamentoModel a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND a.dataAgendamento = :dataAgendamento " +
            "AND a.status != :status")
    List<AgendamentoModel> findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
            @Param("cdMecanico") Integer cdMecanico,
            @Param("dataAgendamento") LocalDate dataAgendamento,
            @Param("status") Status status
    );

    // ✅ NOVO: Buscar agendamentos vinculados a uma Ordem de Serviço
    @Query("SELECT a FROM AgendamentoModel a WHERE a.ordemServico.cdOrdemServico = :cdOrdemServico")
    List<AgendamentoModel> findByOrdemServico_CdOrdemServico(@Param("cdOrdemServico") Integer cdOrdemServico);
}