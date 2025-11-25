package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AgendamentoRequestDTO(
        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotNull(message = "Veículo é obrigatório")
        Integer cdVeiculo,

        @NotNull(message = "Mecânico é obrigatório")
        Integer cdMecanico,

        @NotNull(message = "Horário é obrigatório")
        @Future(message = "Horário deve ser no futuro")
        LocalDateTime horario,

        @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
        String observacoes,

        StatusAgendamento status
) {}