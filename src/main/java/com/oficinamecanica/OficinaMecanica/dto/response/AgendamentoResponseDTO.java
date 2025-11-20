package com.oficinamecanica.OficinaMecanica.dto.response;

import com.oficinamecanica.OficinaMecanica.models.Agendamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoResponseDTO {
    private Integer cdAgendamento;
    private Integer cdCliente;
    private String nmCliente;
    private Integer cdVeiculo;
    private String placa;
    private Integer cdMecanico;
    private String nmMecanico;
    private LocalDateTime horario;
    private Agendamento.StatusAgendamento status;
    private String observacoes;
    private LocalDateTime dataAgendamento;
}
