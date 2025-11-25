package com.oficinamecanica.OficinaMecanica.dto.response;

import java.time.LocalDateTime;

public record VeiculoResponseDTO(
        Integer cdVeiculo,
        Integer cdCliente,
        String nmCliente,
        String placa,
        String modelo,
        String marca,
        Integer ano,
        String cor,
        LocalDateTime dataCadastro
) {}