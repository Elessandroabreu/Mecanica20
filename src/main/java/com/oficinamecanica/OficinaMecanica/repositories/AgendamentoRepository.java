package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    // Listar agendamentos por status
    List<Agendamento> findByStatus(StatusAgendamento status);

    // Listar agendamentos de um mecânico
    List<Agendamento> findByMecanico_CdUsuario(Integer cdMecanico);

    // Listar agendamentos de um cliente
    List<Agendamento> findByCliente_CdCliente(Integer cdCliente);

    // Listar agendamentos de um veículo
    List<Agendamento> findByVeiculo_CdVeiculo(Integer cdVeiculo);

    // Listar agendamentos futuros (a partir de hoje)
    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento >= :dataAtual AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosFuturos(@Param("dataAtual") LocalDateTime dataAtual);

    // Verificar se mecânico já tem agendamento nessa data
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND DATE(a.dataAgendamento) = :data AND a.status = 'AGENDADO'")
    boolean existeAgendamentoNaData(@Param("cdMecanico") Integer cdMecanico,
                                    @Param("data") LocalDate data);

    // Listar agendamentos do mecânico em um período
    @Query("SELECT a FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND a.dataAgendamento BETWEEN :dataInicio AND :dataFim " +
            "ORDER BY a.dataAgendamento ASC")
    List<Agendamento> findAgendamentosMecanicoNoPeriodo(@Param("cdMecanico") Integer cdMecanico,
                                                        @Param("dataInicio") LocalDateTime dataInicio,
                                                        @Param("dataFim") LocalDateTime dataFim);

    // Listar agendamentos do mecânico em uma data específica
    @Query("SELECT a FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND DATE(a.dataAgendamento) = :data AND a.status = 'AGENDADO' " +
            "ORDER BY a.dataAgendamento ASC")
    List<Agendamento> findAgendamentosDoDia(@Param("cdMecanico") Integer cdMecanico,
                                            @Param("data") LocalDate data);

    // ===== MÉTODOS ADICIONAIS ÚTEIS =====

    // Listar todos os agendamentos de uma data específica
    @Query("SELECT a FROM Agendamento a WHERE DATE(a.dataAgendamento) = :data " +
            "ORDER BY a.dataAgendamento ASC")
    List<Agendamento> findAgendamentosPorData(@Param("data") LocalDate data);

    // Listar agendamentos de hoje
    @Query("SELECT a FROM Agendamento a WHERE DATE(a.dataAgendamento) = CURRENT_DATE " +
            "AND a.status = 'AGENDADO' ORDER BY a.dataAgendamento ASC")
    List<Agendamento> findAgendamentosDeHoje();

    // Contar agendamentos do mecânico em uma data
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.mecanico.cdUsuario = :cdMecanico " +
            "AND DATE(a.dataAgendamento) = :data AND a.status = 'AGENDADO'")
    Long contarAgendamentosMecanicoPorData(@Param("cdMecanico") Integer cdMecanico,
                                           @Param("data") LocalDate data);

    // Listar agendamentos pendentes (AGENDADO) de um cliente
    @Query("SELECT a FROM Agendamento a WHERE a.cliente.cdCliente = :cdCliente " +
            "AND a.status = 'AGENDADO' ORDER BY a.dataAgendamento ASC")
    List<Agendamento> findAgendamentosPendentesCliente(@Param("cdCliente") Integer cdCliente);

    // Listar agendamentos por veículo e status
    List<Agendamento> findByVeiculo_CdVeiculoAndStatus(Integer cdVeiculo, StatusAgendamento status);
}