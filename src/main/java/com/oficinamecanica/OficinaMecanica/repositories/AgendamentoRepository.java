package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    // Listar agendamentos por status
    List<Agendamento> findByStatus(Agendamento.StatusAgendamento status);

    // Listar agendamentos de um mecânico
    List<Agendamento> findByMecanico_CdUsuario(Integer cdMecanico);

    // Listar agendamentos de um cliente
    List<Agendamento> findByCliente_CdCliente(Integer cdCliente);

    // Listar agendamentos de um veículo
    List<Agendamento> findByVeiculo_CdVeiculo(Integer cdVeiculo);

    // Listar agendamentos futuros
    @Query("SELECT a FROM Agendamento a WHERE a.horario >= :dataAtual AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosFuturos(@Param("dataAtual") LocalDateTime dataAtual);

    // Verificar disponibilidade do mecânico em determinado horário
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
           "AND a.horario = :horario AND a.status = 'AGENDADO'")
    boolean existsAgendamentoNoHorario(@Param("cdMecanico") Integer cdMecanico, 
                                       @Param("horario") LocalDateTime horario);

    // Listar agendamentos do mecânico em um período
    @Query("SELECT a FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
           "AND a.horario BETWEEN :dataInicio AND :dataFim")
    List<Agendamento> findAgendamentosMecanicoNoPeriodo(@Param("cdMecanico") Integer cdMecanico,
                                                         @Param("dataInicio") LocalDateTime dataInicio,
                                                         @Param("dataFim") LocalDateTime dataFim);

    // Listar agendamentos do dia para um mecânico
    @Query("SELECT a FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
           "AND DATE(a.horario) = DATE(:data) AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosDoDia(@Param("cdMecanico") Integer cdMecanico,
                                             @Param("data") LocalDateTime data);
}
