package com.oficinamecanica.OficinaMecanica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ServicoDTO(
        Integer cdServico,

        @NotBlank(message = "Nome do serviço é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nmServico,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String dsServico,

        @NotNull(message = "Valor do serviço é obrigatório")
        @Positive(message = "Valor do serviço deve ser positivo")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        Double vlServico,

        Boolean ativo
) {}