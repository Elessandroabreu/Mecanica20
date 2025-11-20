package com.oficinamecanica.OficinaMecanica.dto.request;

import com.oficinamecanica.OficinaMecanica.enums.StatusAgendamento;
import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoRequestDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Integer cdCliente;

    @NotNull(message = "Veículo é obrigatório")
    private Integer cdVeiculo;

    @NotNull(message = "Mecânico é obrigatório")
    private Integer cdMecanico;

    @NotNull(message = "Horário é obrigatório")
    @Future(message = "Horário deve ser no futuro")
    private LocalDateTime horario;

    @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
    private String observacoes;

    private StatusAgendamento status;
}
