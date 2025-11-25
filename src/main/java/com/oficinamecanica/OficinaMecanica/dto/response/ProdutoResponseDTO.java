package com.oficinamecanica.OficinaMecanica.dto.response;

import java.time.LocalDateTime;

public record ProdutoResponseDTO(
        Integer cdProduto,
        String nmProduto,
        String dsProduto,
        String categoria,
        Double vlCusto,
        Double vlVenda,
        Integer qtdEstoque,
        Integer qtdMinimo,
        Boolean ativo,
        LocalDateTime dataCadastro
) {}