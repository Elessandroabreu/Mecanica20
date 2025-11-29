package com.oficinamecanica.OficinaMecanica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VeiculoDTO(
        Integer cdVeiculo,

        @NotNull(message = "Cliente é obrigatório")
        Integer cdCliente,

        @NotBlank(message = "Placa é obrigatória")
        @Size(max = 10, message = "Placa deve ter no máximo 10 caracteres")
        String placa,

        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres")
        String modelo,

        @NotBlank(message = "Marca é obrigatória")
        @Size(max = 50, message = "Marca deve ter no máximo 50 caracteres")
        String marca,

        @NotNull(message = "Ano é obrigatório")
        Integer ano,

        @Size(max = 30, message = "Cor deve ter no máximo 30 caracteres")
        String cor
) {}