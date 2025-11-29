package com.oficinamecanica.OficinaMecanica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoDTO(
        Integer cdProduto,

        @NotBlank(message = "Nome do produto é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nmProduto,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String dsProduto,

        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
        String categoria,

        @NotNull(message = "Valor de venda é obrigatório")
        @Positive(message = "Valor de venda deve ser positivo")
        Double vlProduto,

        @NotNull(message = "Quantidade em estoque é obrigatória")
        @PositiveOrZero(message = "Quantidade em estoque deve ser zero ou positiva")
        Integer qtdEstoque,

        @NotNull(message = "Quantidade mínima é obrigatória")
        @PositiveOrZero(message = "Quantidade mínima deve ser zero ou positiva")
        Integer qtdMinimoEstoque,

        Boolean ativo
) {}

