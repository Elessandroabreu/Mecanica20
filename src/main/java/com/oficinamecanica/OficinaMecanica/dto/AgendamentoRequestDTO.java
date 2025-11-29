package com.oficinamecanica.OficinaMecanica.dto;

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

        @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
        String observacoes
) {}