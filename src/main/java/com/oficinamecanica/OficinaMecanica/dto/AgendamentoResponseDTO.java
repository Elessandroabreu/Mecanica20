package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.Status;
import java.time.LocalDate;

public record AgendamentoResponseDTO(
        Integer cdAgendamento,
        Integer cdCliente,
        String nmCliente,
        String cpfCliente,
        String telefoneCliente,
        Integer cdVeiculo,
        String placaVeiculo,
        String modeloVeiculo,
        String marcaVeiculo,
        Integer cdMecanico,
        String nmMecanico,
        LocalDate dataAgendamento,
        Status status,
        String observacoes,
        Integer cdOrdemServico
) {}