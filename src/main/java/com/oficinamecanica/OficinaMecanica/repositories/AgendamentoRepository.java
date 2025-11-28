package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByStatus(StatusAgendamento status);

    List<Agendamento> findByMecanico_CdUsuario(Integer cdMecanico);

    List<Agendamento> findByCliente_CdCliente(Integer cdCliente);

    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento >= :dataAtual AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosFuturos(@Param("dataAtual") LocalDate dataAtual);

    // ✅ Buscar agendamentos do mecânico em uma data (exceto cancelados)
    @Query("SELECT a FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND a.dataAgendamento = :dataAgendamento " +
            "AND a.status != :status")
    List<Agendamento> findByMecanico_CdUsuarioAndDataAgendamentoAndStatusNot(
            @Param("cdMecanico") Integer cdMecanico,
            @Param("dataAgendamento") LocalDate dataAgendamento,
            @Param("status") StatusAgendamento status
    );

    // ✅ NOVO: Buscar agendamentos vinculados a uma Ordem de Serviço
    @Query("SELECT a FROM Agendamento a WHERE a.ordemServico.cdOrdemServico = :cdOrdemServico")
    List<Agendamento> findByOrdemServico_CdOrdemServico(@Param("cdOrdemServico") Integer cdOrdemServico);
}