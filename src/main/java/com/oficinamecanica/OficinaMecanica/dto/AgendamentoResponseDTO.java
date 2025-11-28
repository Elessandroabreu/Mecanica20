package com.oficinamecanica.OficinaMecanica.dto;

import com.oficinamecanica.OficinaMecanica.enums.Status;

import java.time.LocalDate;

record AgendamentoResponseDTO(
        Integer cdAgendamento,
        Integer cdCliente,
        String nomeCliente,
        String cpfCliente,
        String telefoneCliente,
        Integer cdVeiculo,
        String placaVeiculo,
        String modeloVeiculo,
        String marcaVeiculo,
        Integer cdMecanico,
        String nomeMecanico,
        LocalDate dataAgendamento,
        Status status,
        String observacoes,
        Integer cdOrdemServico // Null se ainda não gerou OS
) {}