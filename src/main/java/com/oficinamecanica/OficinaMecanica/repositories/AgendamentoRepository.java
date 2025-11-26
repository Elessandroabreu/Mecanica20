package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
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
    List<Agendamento> findByStatus(StatusAgendamento status);

    // Listar agendamentos de um mecânico
    List<Agendamento> findByMecanico_CdUsuario(Integer cdMecanico);

    // Listar agendamentos de um cliente
    List<Agendamento> findByCliente_CdCliente(Integer cdCliente);

    // Listar agendamentos futuros (a partir de hoje)
    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento >= :dataAtual AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosFuturos(@Param("dataAtual") LocalDateTime dataAtual);
}