package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import java.time.LocalDate; // ✅ MUDOU: Era LocalDateTime

public record AgendamentoResponseDTO(
        Integer cdAgendamento,
        Integer cdCliente,
        String nmCliente,
        Integer cdVeiculo,
        String placa,
        Integer cdMecanico,
        String nmMecanico,
        StatusAgendamento status,
        String observacoes,
        LocalDate dataAgendamento
) {}