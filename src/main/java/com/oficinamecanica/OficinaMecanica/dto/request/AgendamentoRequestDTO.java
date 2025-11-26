package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AgendamentoRequestDTO(

        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotNull(message = "Veículo é obrigatório")
        Integer cdVeiculo,

        @NotNull(message = "Mecânico é obrigatório")
        Integer cdMecanico,

        @NotNull(message = "Data é obrigatória")
        LocalDate dataAgendamento,

        String observacoes,

        StatusAgendamento status
) {}