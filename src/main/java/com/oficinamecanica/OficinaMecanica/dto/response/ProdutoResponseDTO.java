package com.oficinamecanica.OficinaMecanica.dto.response;

public record ProdutoResponseDTO(
        Integer cdProduto,
        String nmProduto,
        String dsProduto,
        String categoria,
        Double vlCusto,
        Double vlVenda,
        Integer qtdEstoque,
        Integer qtdMinimo,
        Boolean ativo
) {}