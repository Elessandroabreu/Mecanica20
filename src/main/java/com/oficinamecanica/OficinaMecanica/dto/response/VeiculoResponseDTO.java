package com.oficinamecanica.OficinaMecanica.dto.response;

public record VeiculoResponseDTO(
        Integer cdVeiculo,
        Integer cdCliente,
        String nmCliente,
        String placa,
        String modelo,
        String marca,
        Integer ano,
        String cor
) {}