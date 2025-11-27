package com.oficinamecanica.OficinaMecanica.repositories;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // ✅ MUDOU
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {


    List<Agendamento> findByStatus(StatusAgendamento status);

    List<Agendamento> findByMecanico_CdUsuario(Integer cdMecanico);

    List<Agendamento> findByCliente_CdCliente(Integer cdCliente);

    @Query("SELECT a FROM Agendamento a WHERE a.dataAgendamento >= :dataAtual AND a.status = 'AGENDADO'")
    List<Agendamento> findAgendamentosFuturos(@Param("dataAtual") LocalDate dataAtual);
}