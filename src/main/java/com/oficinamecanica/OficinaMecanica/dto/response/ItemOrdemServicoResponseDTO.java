package com.oficinamecanica.OficinaMecanica.dto.response;

public record ItemOrdemServicoResponseDTO(
        Integer cdItemOrdemServico,
        Integer cdProduto,
        String nmProduto,
        Integer cdServico,
        String nmServico,
        Integer quantidade,
        Double vlUnitario,
        Double vlTotal
) {}